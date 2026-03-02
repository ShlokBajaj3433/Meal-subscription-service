package com.mealsubscription.dto.response;

import com.mealsubscription.entity.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
    Long id,
    Long subscriptionId,
    String stripePaymentIntentId,
    String stripeInvoiceId,
    Long amountCents,
    String currency,
    PaymentStatus status,
    String failureMessage,
    LocalDateTime paidAt,
    LocalDateTime createdAt
) {}
