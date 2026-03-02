package com.mealsubscription.dto.request;

import com.mealsubscription.entity.PlanType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record SubscriptionRequest(

    @NotNull(message = "Plan type is required")
    PlanType planType,

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    LocalDate startDate,

    /** Pre-selected meal slots submitted during checkout */
    @NotNull
    @Size(min = 1, message = "Select at least one meal")
    List<@Valid MealSlotRequest> mealSlots
) {

    public record MealSlotRequest(

        @NotNull(message = "Meal ID is required")
        Long mealId,

        @NotNull(message = "Delivery date is required")
        LocalDate deliveryDate,

        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 5, message = "Maximum 5 of the same meal per day")
        int quantity
    ) {}
}
