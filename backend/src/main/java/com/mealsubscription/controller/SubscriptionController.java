package com.mealsubscription.controller;

import com.mealsubscription.dto.request.SubscriptionRequest;
import com.mealsubscription.dto.response.SubscriptionResponse;
import com.mealsubscription.service.SubscriptionService;
import com.mealsubscription.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@AuthenticationPrincipal UserDetails principal,
                                       @Valid @RequestBody SubscriptionRequest request) {
        Long userId = userService.getByEmail(principal.getUsername()).id();
        return subscriptionService.create(userId, request);
    }

    @GetMapping
    public Page<SubscriptionResponse> mySubscriptions(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = userService.getByEmail(principal.getUsername()).id();
        return subscriptionService.listByUser(userId, pageable);
    }

    @GetMapping("/{id}")
    public SubscriptionResponse getById(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails principal) {
        return subscriptionService.getById(id);
    }

    @PostMapping("/{id}/pause")
    public SubscriptionResponse pause(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.getByEmail(principal.getUsername()).id();
        return subscriptionService.pause(id, userId);
    }

    @PostMapping("/{id}/resume")
    public SubscriptionResponse resume(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.getByEmail(principal.getUsername()).id();
        return subscriptionService.resume(id, userId);
    }

    @PostMapping("/{id}/cancel")
    public SubscriptionResponse cancel(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.getByEmail(principal.getUsername()).id();
        return subscriptionService.cancel(id, userId);
    }
}
