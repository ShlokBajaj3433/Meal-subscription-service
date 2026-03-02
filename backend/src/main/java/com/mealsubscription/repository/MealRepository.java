package com.mealsubscription.repository;

import com.mealsubscription.entity.DietaryType;
import com.mealsubscription.entity.Meal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {

    Page<Meal> findByDietaryTypeAndAvailable(DietaryType dietaryType, boolean available, Pageable pageable);

    Page<Meal> findByAvailable(boolean available, Pageable pageable);

    @Query("SELECT m FROM Meal m WHERE m.available = true " +
           "AND (:dietary IS NULL OR m.dietaryType = :dietary) " +
           "ORDER BY m.name ASC")
    Page<Meal> findAvailableByOptionalDietaryType(
        @Param("dietary") DietaryType dietary, Pageable pageable);

    List<Meal> findByAvailableTrue();
}
