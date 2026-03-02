package com.mealsubscription.controller;

import com.mealsubscription.dto.response.SubscriptionResponse;
import com.mealsubscription.dto.response.UserResponse;
import com.mealsubscription.service.SubscriptionService;
import com.mealsubscription.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")       // applied to all methods in this controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    // ── Users ─────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public Page<UserResponse> listUsers(@PageableDefault(size = 20) Pageable pageable) {
        return userService.listAll(pageable);
    }

    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getById(id);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(@PathVariable Long id) {
        userService.deactivate(id);
    }

    // ── Subscriptions ─────────────────────────────────────────────────────

    @GetMapping("/subscriptions")
    public Page<SubscriptionResponse> listAllSubscriptions(
            @PageableDefault(size = 20) Pageable pageable) {
        return subscriptionService.listAll(pageable);
    }

    @GetMapping("/subscriptions/{id}")
    public SubscriptionResponse getSubscription(@PathVariable Long id) {
        return subscriptionService.getById(id);
    }
}
