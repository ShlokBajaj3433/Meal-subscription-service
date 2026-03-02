package com.mealsubscription.service;

import com.mealsubscription.dto.response.PaymentResponse;
import com.stripe.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    /** Called by the webhook controller after Stripe signature is verified */
    void handleStripeEvent(Event event);

    /** Retrieve payment history for the currently authenticated user */
    Page<PaymentResponse> listPaymentsForUser(Long userId, Pageable pageable);

    /** Admin — all payments */
    Page<PaymentResponse> listAll(Pageable pageable);
}
