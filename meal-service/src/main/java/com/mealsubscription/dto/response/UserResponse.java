package com.mealsubscription.dto.response;

import com.mealsubscription.entity.Role;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String name,
    String email,
    Role role,
    boolean active,
    LocalDateTime createdAt
) {}
