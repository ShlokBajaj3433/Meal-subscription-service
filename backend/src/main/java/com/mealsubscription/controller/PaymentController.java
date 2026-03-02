package com.mealsubscription.controller;

import com.mealsubscription.dto.response.PaymentResponse;
import com.mealsubscription.exception.PaymentException;
import com.mealsubscription.service.PaymentService;
import com.mealsubscription.service.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    /**
     * Stripe webhook endpoint.
     *
     * Security: Stripe-Signature header is always validated with
     * {@link Webhook#constructEvent} before any business logic runs.
     * The raw String body must NOT be parsed by Jackson first — Spring is
     * configured to read this endpoint as text/plain so the raw bytes are preserved.
     *
     * This endpoint is permitAll() in SecurityConfig because Stripe cannot
     * provide a JWT; instead, the HMAC-SHA256 signature IS the authentication.
     */
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawPayload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(rawPayload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error parsing Stripe webhook payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            paymentService.handleStripeEvent(event);
        } catch (Exception e) {
            // Return 200 to Stripe so it doesn't retry for non-retryable errors.
            // Idempotency logic prevents double-processing if Stripe does retry.
            log.error("Error handling Stripe event id={}: {}", event.getId(), e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Retrieve payment history for the authenticated user.
     */
    @GetMapping
    public Page<PaymentResponse> myPayments(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = userService.getByEmail(principal.getUsername()).id();
        return paymentService.listPaymentsForUser(userId, pageable);
    }

    /**
     * Admin — all payments across all users.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PaymentResponse> allPayments(@PageableDefault(size = 20) Pageable pageable) {
        return paymentService.listAll(pageable);
    }
}
