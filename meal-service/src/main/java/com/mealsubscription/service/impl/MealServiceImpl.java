package com.mealsubscription.service.impl;

import com.mealsubscription.dto.request.MealRequest;
import com.mealsubscription.dto.response.MealResponse;
import com.mealsubscription.entity.DietaryType;
import com.mealsubscription.entity.Meal;
import com.mealsubscription.exception.ResourceNotFoundException;
import com.mealsubscription.repository.MealRepository;
import com.mealsubscription.service.MealService;
import com.mealsubscription.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public MealResponse create(MealRequest request) {
        Meal meal = buildMealFromRequest(new Meal(), request);
        Meal saved = mealRepository.save(meal);
        log.info("Meal created: id={}, name={}", saved.getId(), saved.getName());
        return mapper.toMealResponse(saved);
    }

    @Override
    @Transactional
    public MealResponse update(Long id, MealRequest request) {
        Meal meal = findById(id);
        buildMealFromRequest(meal, request);
        return mapper.toMealResponse(mealRepository.save(meal));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Meal meal = findById(id);
        meal.setAvailable(false);          // soft-delete: mark unavailable, keep history
        mealRepository.save(meal);
        log.info("Meal soft-deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public MealResponse getById(Long id) {
        return mapper.toMealResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MealResponse> listAvailable(DietaryType dietary, Pageable pageable) {
        return mealRepository
            .findAvailableByOptionalDietaryType(dietary, pageable)
            .map(mapper::toMealResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MealResponse> listAll(Pageable pageable) {
        return mealRepository.findAll(pageable).map(mapper::toMealResponse);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Meal findById(Long id) {
        return mealRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.forEntity("Meal", id));
    }

    private Meal buildMealFromRequest(Meal meal, MealRequest req) {
        meal.setName(req.name());
        meal.setDescription(req.description());
        meal.setDietaryType(req.dietaryType());
        meal.setCalories(req.calories());
        meal.setPriceCents(req.priceCents());
        meal.setImageUrl(req.imageUrl());
        meal.setAvailable(req.available());
        return meal;
    }
}
