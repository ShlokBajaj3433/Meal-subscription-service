package com.mealsubscription.service;

import com.mealsubscription.dto.request.RegisterRequest;
import com.mealsubscription.dto.request.UpdateProfileRequest;
import com.mealsubscription.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse getById(Long id);
    UserResponse getByEmail(String email);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    void deactivate(Long userId);
    Page<UserResponse> listAll(Pageable pageable);
}
