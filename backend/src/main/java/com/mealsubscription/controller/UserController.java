package com.mealsubscription.controller;

import com.mealsubscription.dto.request.UpdateProfileRequest;
import com.mealsubscription.dto.response.UserResponse;
import com.mealsubscription.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getProfile(@AuthenticationPrincipal UserDetails principal) {
        return userService.getByEmail(principal.getUsername());
    }

    @PatchMapping("/me")
    public UserResponse updateProfile(@AuthenticationPrincipal UserDetails principal,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse me = userService.getByEmail(principal.getUsername());
        return userService.updateProfile(me.id(), request);
    }
}
