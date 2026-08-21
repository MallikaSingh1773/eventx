package com.eventx.service.impl;

import com.eventx.dto.request.RefundRequest;
import com.eventx.dto.response.RefundResponse;
import com.eventx.entity.*;
import com.eventx.entity.enums.BookingStatus;
import com.eventx.entity.enums.PaymentStatus;
import com.eventx.entity.enums.RefundStatus;
import com.eventx.entity.enums.TicketStatus;
import com.eventx.exception.BadRequestException;
import com.eventx.exception.ResourceNotFoundException;
import com.eventx.repository.*;
import com.eventx.service.RefundService;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final com.eventx.repository.RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final RazorpayClient razorpayClient;

    @Override
    public RefundResponse processRefund(Long bookingId, RefundRequest request, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            // Can be admin too, but user validation logic here
            throw new BadRequestException("User does not own this booking");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed bookings can be refunded");
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BadRequestException("No successful payment found for this booking"));

        // Cancellation policy (event > 24 hours away)
        if (booking.getEvent().getEventDate().atStartOfDay().minusHours(24).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cancellation is not allowed within 24 hours of the event");
        }

        booking.setStatus(BookingStatus.REFUND_PENDING);
        bookingRepository.save(booking);

        try {
            BigDecimal amount = payment.getAmount();
            long amountInPaise = amount.multiply(new BigDecimal(100)).longValue();

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amountInPaise);
            refundRequest.put("speed", "normal");
            refundRequest.put("payment_id", payment.getRazorpayPaymentId());

            Refund razorpayRefund = razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            com.eventx.entity.Refund refund = new com.eventx.entity.Refund();
            refund.setBooking(booking);
            refund.setPayment(payment);
            refund.setRazorpayRefundId(razorpayRefund.get("id"));
            refund.setAmount(amount);
            refund.setReason(request.getReason());
            refund.setStatus(RefundStatus.PROCESSED);
            
            com.eventx.entity.Refund savedRefund = refundRepository.save(refund);

            booking.setStatus(BookingStatus.REFUNDED);
            bookingRepository.save(booking);

            // Cancel tickets and restore capacity
            List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
            for (Ticket ticket : tickets) {
                ticket.setStatus(TicketStatus.CANCELLED);
                ticketRepository.save(ticket);
                
                TicketCategory category = ticket.getBookingItem().getTicketCategory();
                category.setAvailableSeats(category.getAvailableSeats() + 1);
                ticketCategoryRepository.save(category);
            }

            return mapToRefundResponse(savedRefund);

        } catch (Exception e) {
            log.error("Refund failed for booking ID: {}", bookingId, e);
            throw new RuntimeException("Payment refund failed: " + e.getMessage());
        }
    }

    @Override
    public List<RefundResponse> getRefundsForBooking(Long bookingId) {
        return refundRepository.findByBookingId(bookingId).stream()
                .map(this::mapToRefundResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RefundResponse> getAllRefunds(Pageable pageable) {
        return refundRepository.findAll(pageable).map(this::mapToRefundResponse);
    }

    private RefundResponse mapToRefundResponse(com.eventx.entity.Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .bookingId(refund.getBooking().getId())
                .paymentId(refund.getPayment().getId())
                .razorpayRefundId(refund.getRazorpayRefundId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .build();
    }
}

