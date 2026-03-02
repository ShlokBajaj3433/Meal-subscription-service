package com.mealsubscription.dto.response;

import com.mealsubscription.entity.PlanType;
import com.mealsubscription.entity.SubscriptionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SubscriptionResponse(
    Long id,
    Long userId,
    String userEmail,
    PlanType planType,
    SubscriptionStatus status,
    LocalDate startDate,
    LocalDate endDate,
    String stripeSubscriptionId,
    List<MealSlotResponse> mealSlots,
    LocalDateTime createdAt
) {

    public record MealSlotResponse(
        Long id,
        Long mealId,
        String mealName,
        LocalDate deliveryDate,
        int quantity
    ) {}
}
