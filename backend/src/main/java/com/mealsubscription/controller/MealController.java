package com.mealsubscription.controller;

import com.mealsubscription.dto.request.MealRequest;
import com.mealsubscription.dto.response.MealResponse;
import com.mealsubscription.entity.DietaryType;
import com.mealsubscription.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    /** Public — browse available meals, optionally filtered by dietary type */
    @GetMapping
    public Page<MealResponse> listMeals(
            @RequestParam(required = false) DietaryType dietary,
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {
        return mealService.listAvailable(dietary, pageable);
    }

    /** Public — view a single meal's details */
    @GetMapping("/{id}")
    public MealResponse getMeal(@PathVariable Long id) {
        return mealService.getById(id);
    }

    /** Admin only — create a new meal */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public MealResponse createMeal(@Valid @RequestBody MealRequest request) {
        return mealService.create(request);
    }

    /** Admin only — update a meal */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MealResponse updateMeal(@PathVariable Long id,
                                   @Valid @RequestBody MealRequest request) {
        return mealService.update(id, request);
    }

    /** Admin only — soft-delete (marks unavailable) */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMeal(@PathVariable Long id) {
        mealService.delete(id);
    }
}
