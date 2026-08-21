package com.eventx.controller;

import com.eventx.exception.PaymentException;
import com.eventx.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Webhooks")
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        try {
            paymentService.handleWebhook(payload, signature);
            return ResponseEntity.ok().build();
        } catch (PaymentException e) {
            log.warn("Rejected webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error handling webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
