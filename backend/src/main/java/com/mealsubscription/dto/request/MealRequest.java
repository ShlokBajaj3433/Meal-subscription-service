package com.mealsubscription.dto.request;

import com.mealsubscription.entity.DietaryType;
import jakarta.validation.constraints.*;

public record MealRequest(

    @NotBlank(message = "Meal name is required")
    @Size(min = 2, max = 200)
    String name,

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String description,

    @NotNull(message = "Dietary type is required")
    DietaryType dietaryType,

    @Min(value = 1, message = "Calories must be positive")
    @Max(value = 5000, message = "Calories cannot exceed 5000")
    Integer calories,

    @NotNull(message = "Price is required")
    @Min(value = 1, message = "Price must be at least 1 cent")
    Long priceCents,

    @Size(max = 500)
    String imageUrl,

    boolean available
) {}
