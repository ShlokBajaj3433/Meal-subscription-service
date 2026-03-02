package com.mealsubscription.service;

import com.mealsubscription.dto.request.MealRequest;
import com.mealsubscription.dto.response.MealResponse;
import com.mealsubscription.entity.DietaryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MealService {
    MealResponse create(MealRequest request);
    MealResponse update(Long id, MealRequest request);
    void delete(Long id);
    MealResponse getById(Long id);
    Page<MealResponse> listAvailable(DietaryType dietary, Pageable pageable);
    Page<MealResponse> listAll(Pageable pageable);
}
