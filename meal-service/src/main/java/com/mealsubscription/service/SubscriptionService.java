package com.mealsubscription.service;

import com.mealsubscription.dto.request.SubscriptionRequest;
import com.mealsubscription.dto.response.SubscriptionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {
    SubscriptionResponse create(Long userId, SubscriptionRequest request);
    SubscriptionResponse getById(Long id);
    Page<SubscriptionResponse> listByUser(Long userId, Pageable pageable);
    SubscriptionResponse pause(Long id, Long userId);
    SubscriptionResponse resume(Long id, Long userId);
    SubscriptionResponse cancel(Long id, Long userId);
    Page<SubscriptionResponse> listAll(Pageable pageable);   // admin
}
