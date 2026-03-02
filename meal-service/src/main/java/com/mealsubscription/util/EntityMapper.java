package com.mealsubscription.util;

import com.mealsubscription.dto.response.MealResponse;
import com.mealsubscription.dto.response.PaymentResponse;
import com.mealsubscription.dto.response.SubscriptionResponse;
import com.mealsubscription.dto.response.UserResponse;
import com.mealsubscription.entity.Meal;
import com.mealsubscription.entity.Payment;
import com.mealsubscription.entity.Subscription;
import com.mealsubscription.entity.SubscriptionMeal;
import com.mealsubscription.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    // ── User ────────────────────────────────────────────────────────────────
    @Mapping(source = "active", target = "active")
    UserResponse toUserResponse(User user);

    // ── Meal ────────────────────────────────────────────────────────────────
    MealResponse toMealResponse(Meal meal);

    List<MealResponse> toMealResponseList(List<Meal> meals);

    // ── Subscription ────────────────────────────────────────────────────────
    @Mapping(source = "user.id",    target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "subscriptionMeals", target = "mealSlots")
    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    @Mapping(source = "meal.id",   target = "mealId")
    @Mapping(source = "meal.name", target = "mealName")
    SubscriptionResponse.MealSlotResponse toMealSlotResponse(SubscriptionMeal sm);

    // ── Payment ─────────────────────────────────────────────────────────────
    @Mapping(source = "subscription.id", target = "subscriptionId")
    PaymentResponse toPaymentResponse(Payment payment);
}
