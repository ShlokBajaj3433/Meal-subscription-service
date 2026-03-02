package com.mealsubscription.dto.response;

import com.mealsubscription.entity.Role;

public record AuthResponse(
    String token,
    String tokenType,
    Long expiresInMs,
    Long userId,
    String email,
    Role role
) {
    public static AuthResponse of(String token, long expiresInMs,
                                   Long userId, String email, Role role) {
        return new AuthResponse(token, "Bearer", expiresInMs, userId, email, role);
    }
}
