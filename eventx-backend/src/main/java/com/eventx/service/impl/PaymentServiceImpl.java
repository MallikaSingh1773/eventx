package com.eventx.service.impl;

import com.eventx.dto.request.CreateOrderRequest;
import com.eventx.dto.request.PaymentVerificationRequest;
import com.eventx.dto.response.OrderResponse;
import com.eventx.dto.response.PaymentResponse;
import com.eventx.entity.Booking;
import com.eventx.entity.Payment;
import com.eventx.entity.enums.BookingStatus;
import com.eventx.entity.enums.PaymentStatus;
import com.eventx.exception.BookingException;
import com.eventx.exception.PaymentException;
import com.eventx.exception.ResourceNotFoundException;
import com.eventx.repository.BookingRepository;
import com.eventx.repository.PaymentRepository;
import com.eventx.service.BookingService;
import com.eventx.service.PaymentService;
import com.eventx.service.TicketService;
import com.eventx.util.SignatureUtils;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final TicketService ticketService;

    @Value("${app.razorpay.key-id}")
    private String keyId;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    @Value("${app.razorpay.webhook-secret}")
    private String webhookSecret;

    public PaymentServiceImpl(
            RazorpayClient razorpayClient,
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            BookingService bookingService,
            TicketService ticketService) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.ticketService = ticketService;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BookingException("Access denied to this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new BookingException("Booking is not in a valid state for payment");
        }

        if (booking.getLockedUntil() != null && booking.getLockedUntil().isBefore(LocalDateTime.now())) {
            throw new BookingException("Booking seat lock has expired");
        }

        if (isPlaceholderKey(keyId) || isPlaceholderKey(keySecret)) {
            throw new PaymentException("Razorpay is not configured. Add RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET to .env");
        }

        Payment existingPayment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
        if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BookingException("This booking is already paid");
        }
        if (existingPayment != null && existingPayment.getRazorpayOrderId() != null
                && existingPayment.getStatus() == PaymentStatus.PENDING) {
            return OrderResponse.builder()
                    .razorpayOrderId(existingPayment.getRazorpayOrderId())
                    .amount(booking.getTotalAmount())
                    .amountInPaise(toPaise(booking.getTotalAmount()))
                    .currency("INR")
                    .keyId(keyId)
                    .bookingId(booking.getId())
                    .bookingStatus(booking.getStatus().name())
                    .build();
        }

        int amountInPaise = toPaise(booking.getTotalAmount());

        try {
            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", "booking_" + booking.getId());
            options.put("payment_capture", 1);
            JSONObject notes = new JSONObject();
            notes.put("bookingId", String.valueOf(booking.getId()));
            options.put("notes", notes);
            
            Order order = razorpayClient.orders.create(options);
            String razorpayOrderId = order.get("id");

            Payment payment = existingPayment != null ? existingPayment : new Payment();
            payment.setBooking(booking);
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setAmount(booking.getTotalAmount());
            payment.setCurrency("INR");
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            booking.setRazorpayOrderId(razorpayOrderId);
            booking.setStatus(BookingStatus.PAYMENT_PENDING);
            bookingRepository.save(booking);

            return OrderResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .amount(booking.getTotalAmount())
                    .amountInPaise(amountInPaise)
                    .currency("INR")
                    .keyId(keyId)
                    .bookingId(booking.getId())
                    .bookingStatus(booking.getStatus().name())
                    .build();

        } catch (Exception e) {
            log.error("Failed to create Razorpay order for booking {}", booking.getId(), e);
            String detail = e.getMessage() != null ? e.getMessage() : "Failed to initiate payment";
            throw new PaymentException("Razorpay could not create an order: " + detail);
        }
    }

    @Override
    public PaymentResponse verifyPayment(PaymentVerificationRequest request, Long userId) {
        Payment existingByPaymentId = paymentRepository.findByRazorpayPaymentId(request.getRazorpayPaymentId()).orElse(null);
        if (existingByPaymentId != null && existingByPaymentId.getStatus() == PaymentStatus.SUCCESS) {
            return mapToPaymentResponse(existingByPaymentId);
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order ID: " + request.getRazorpayOrderId()));

        Booking booking = payment.getBooking();
        if (userId != null && !booking.getUser().getId().equals(userId)) {
            throw new PaymentException("Access denied");
        }

        String data = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        boolean isValidSignature = SignatureUtils.verifyRazorpaySignature(data, request.getRazorpaySignature(), keySecret);
        if (!isValidSignature) {
            throw new PaymentException("Invalid payment signature");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment = paymentRepository.save(payment);

        tryFinalizeFromRazorpay(payment);
        return mapToPaymentResponse(reloadPayment(payment.getId()));
    }

    @Override
    public PaymentResponse getStatus(Long bookingId, Long userId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (userId != null && !payment.getBooking().getUser().getId().equals(userId)) {
            throw new PaymentException("Access denied");
        }
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            tryFinalizeFromRazorpay(payment);
            payment = reloadPayment(payment.getId());
        }
        return mapToPaymentResponse(payment);
    }

    @Override
    public PaymentResponse completeDemoPayment(CreateOrderRequest request, Long userId) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new PaymentException("Access denied");
        }
        if (booking.getLockedUntil() != null && booking.getLockedUntil().isBefore(LocalDateTime.now())) {
            throw new BookingException("Booking seat lock has expired");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new BookingException("Booking is not in a valid state for payment");
        }

        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            return mapToPaymentResponse(payment);
        }
        if (payment == null) {
            String suffix = UUID.randomUUID().toString().replace("-", "");
            payment = new Payment();
            payment.setBooking(booking);
            payment.setRazorpayOrderId("demo_order_" + suffix);
            payment.setRazorpayPaymentId("demo_payment_" + suffix);
            payment.setAmount(booking.getTotalAmount());
            payment.setCurrency("INR");
        }
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpaySignature("demo-payment");
        payment = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        bookingService.confirmBooking(booking.getId());
        ticketService.generateTickets(booking.getId());
        return mapToPaymentResponse(payment);
    }

    @Override
    public void handleWebhook(String payload, String signature) {
        if (signature == null || signature.isBlank() || isPlaceholderKey(webhookSecret)) {
            throw new PaymentException("Invalid webhook signature");
        }
        boolean isValid = SignatureUtils.verifyRazorpaySignature(payload, signature, webhookSecret);
        if (!isValid) {
            throw new PaymentException("Invalid webhook signature");
        }

        JSONObject json = new JSONObject(payload);
        String eventType = json.getString("event");

        if ("payment.captured".equals(eventType)) {
            JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String razorpayPaymentId = paymentEntity.getString("id");
            String razorpayOrderId = paymentEntity.getString("order_id");
            int amountPaise = paymentEntity.getInt("amount");

            Payment payment = paymentRepository.findByRazorpayPaymentId(razorpayPaymentId)
                    .or(() -> paymentRepository.findByRazorpayOrderId(razorpayOrderId))
                    .orElse(null);
            if (payment == null) {
                log.warn("Webhook payment.captured for unknown order {}", razorpayOrderId);
                return;
            }
            finalizeCaptured(payment, razorpayPaymentId, amountPaise);
        } else if ("payment.failed".equals(eventType)) {
            JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String razorpayOrderId = paymentEntity.optString("order_id", null);
            if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
                return;
            }
            paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
                if (payment.getStatus() != PaymentStatus.SUCCESS) {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                }
            });
        } else if ("refund.processed".equals(eventType)) {
            JSONObject refundEntity = json.getJSONObject("payload").getJSONObject("refund").getJSONObject("entity");
            String paymentId = refundEntity.getString("payment_id");
            paymentRepository.findByRazorpayPaymentId(paymentId).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            });
        }
        log.info("Processed webhook event: {}", eventType);
    }

    private void tryFinalizeFromRazorpay(Payment payment) {
        if (payment.getRazorpayPaymentId() == null || isPlaceholderKey(keyId) || isPlaceholderKey(keySecret)) {
            return;
        }
        try {
            com.razorpay.Payment fetched = razorpayClient.payments.fetch(payment.getRazorpayPaymentId());
            String status = String.valueOf(fetched.get("status"));
            if ("captured".equalsIgnoreCase(status)) {
                Object amount = fetched.get("amount");
                int amountPaise = amount instanceof Number ? ((Number) amount).intValue() : toPaise(payment.getAmount());
                finalizeCaptured(payment, payment.getRazorpayPaymentId(), amountPaise);
            }
        } catch (Exception e) {
            log.warn("Could not sync Razorpay payment {}: {}", payment.getRazorpayPaymentId(), e.getMessage());
        }
    }

    private void finalizeCaptured(Payment payment, String razorpayPaymentId, int amountPaise) {
        synchronized (("eventx-pay-" + payment.getBooking().getId()).intern()) {
            Payment current = paymentRepository.findById(payment.getId()).orElse(payment);
            if (current.getStatus() != PaymentStatus.SUCCESS) {
                int expectedPaise = toPaise(current.getAmount());
                if (amountPaise != expectedPaise) {
                    log.error("Amount mismatch for booking {}: razorpay={} expected={}",
                            current.getBooking().getId(), amountPaise, expectedPaise);
                    current.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(current);
                    throw new PaymentException("Payment amount does not match booking total");
                }
                current.setRazorpayPaymentId(razorpayPaymentId);
                current.setStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(current);
            }
            bookingService.confirmBooking(current.getBooking().getId());
            ticketService.generateTickets(current.getBooking().getId());
        }
    }

    private Payment reloadPayment(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        Booking booking = payment.getBooking();
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(booking.getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .bookingStatus(booking.getStatus() != null ? booking.getStatus().name() : null)
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private int toPaise(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100")).setScale(0, java.math.RoundingMode.HALF_UP).intValue();
    }

    private boolean isPlaceholderKey(String value) {
        if (value == null || value.isBlank()) return true;
        String normalized = value.toLowerCase();
        return normalized.contains("placeholder") || normalized.contains("xxxxxxxx");
    }
}

