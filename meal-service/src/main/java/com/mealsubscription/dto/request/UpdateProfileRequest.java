package com.mealsubscription.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    @Size(min = 8, max = 128)
    String currentPassword,

    @Size(min = 8, max = 128)
    String newPassword
) {}
