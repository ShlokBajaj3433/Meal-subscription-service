package com.mealsubscription.dto.response;

import com.mealsubscription.entity.DietaryType;

import java.time.LocalDateTime;

public record MealResponse(
    Long id,
    String name,
    String description,
    DietaryType dietaryType,
    Integer calories,
    Long priceCents,
    String imageUrl,
    boolean available,
    LocalDateTime createdAt
) {}
