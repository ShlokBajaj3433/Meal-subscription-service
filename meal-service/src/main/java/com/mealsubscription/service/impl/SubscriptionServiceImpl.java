package com.mealsubscription.service.impl;

import com.mealsubscription.dto.request.SubscriptionRequest;
import com.mealsubscription.dto.response.SubscriptionResponse;
import com.mealsubscription.entity.*;
import com.mealsubscription.exception.InvalidSubscriptionStateException;
import com.mealsubscription.exception.ResourceNotFoundException;
import com.mealsubscription.repository.MealRepository;
import com.mealsubscription.repository.SubscriptionRepository;
import com.mealsubscription.repository.UserRepository;
import com.mealsubscription.service.SubscriptionService;
import com.mealsubscription.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public SubscriptionResponse create(Long userId, SubscriptionRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.forEntity("User", userId));

        // Calculate end date based on plan type
        LocalDate endDate = request.planType() == PlanType.WEEKLY
            ? request.startDate().plusWeeks(1)
            : request.startDate().plusMonths(1);

        Subscription subscription = Subscription.builder()
            .user(user)
            .planType(request.planType())
            .startDate(request.startDate())
            .endDate(endDate)
            .status(SubscriptionStatus.ACTIVE)
            .build();

        // Resolve meal slots
        List<SubscriptionMeal> slots = request.mealSlots().stream()
            .map(slot -> {
                Meal meal = mealRepository.findById(slot.mealId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Meal", slot.mealId()));
                return SubscriptionMeal.builder()
                    .subscription(subscription)
                    .meal(meal)
                    .deliveryDate(slot.deliveryDate())
                    .quantity(slot.quantity())
                    .build();
            })
            .toList();

        subscription.getSubscriptionMeals().addAll(slots);
        Subscription saved = subscriptionRepository.save(subscription);

        log.info("Subscription created: id={}, user={}, plan={}",
            saved.getId(), userId, request.planType());
        return mapper.toSubscriptionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getById(Long id) {
        return mapper.toSubscriptionResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> listByUser(Long userId, Pageable pageable) {
        return subscriptionRepository.findByUserId(userId, pageable)
            .map(mapper::toSubscriptionResponse);
    }

    @Override
    @Transactional
    public SubscriptionResponse pause(Long id, Long userId) {
        Subscription sub = findAndValidateOwner(id, userId);
        if (!sub.isActive()) {
            throw new InvalidSubscriptionStateException(
                "Only ACTIVE subscriptions can be paused. Current status: " + sub.getStatus());
        }
        sub.setStatus(SubscriptionStatus.PAUSED);
        log.info("Subscription paused: id={}", id);
        return mapper.toSubscriptionResponse(subscriptionRepository.save(sub));
    }

    @Override
    @Transactional
    public SubscriptionResponse resume(Long id, Long userId) {
        Subscription sub = findAndValidateOwner(id, userId);
        if (!sub.isPaused()) {
            throw new InvalidSubscriptionStateException(
                "Only PAUSED subscriptions can be resumed. Current status: " + sub.getStatus());
        }
        sub.setStatus(SubscriptionStatus.ACTIVE);
        log.info("Subscription resumed: id={}", id);
        return mapper.toSubscriptionResponse(subscriptionRepository.save(sub));
    }

    @Override
    @Transactional
    public SubscriptionResponse cancel(Long id, Long userId) {
        Subscription sub = findAndValidateOwner(id, userId);
        if (sub.isCancelled()) {
            throw new InvalidSubscriptionStateException("Subscription is already cancelled.");
        }
        sub.setStatus(SubscriptionStatus.CANCELLED);
        log.warn("Subscription cancelled: id={}, user={}", id, userId);
        return mapper.toSubscriptionResponse(subscriptionRepository.save(sub));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> listAll(Pageable pageable) {
        return subscriptionRepository.findAll(pageable).map(mapper::toSubscriptionResponse);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Subscription findById(Long id) {
        return subscriptionRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.forEntity("Subscription", id));
    }

    private Subscription findAndValidateOwner(Long subId, Long userId) {
        Subscription sub = findById(subId);
        if (!sub.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                "Subscription " + subId + " not found for user " + userId);
        }
        return sub;
    }
}
