package com.mealsubscription.repository;

import com.mealsubscription.entity.SubscriptionMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionMealRepository extends JpaRepository<SubscriptionMeal, Long> {

    List<SubscriptionMeal> findBySubscriptionId(Long subscriptionId);

    List<SubscriptionMeal> findBySubscriptionIdAndDeliveryDateBetween(
        Long subscriptionId, LocalDate from, LocalDate to);

    @Modifying
    @Query("DELETE FROM SubscriptionMeal sm WHERE sm.subscription.id = :subId AND sm.deliveryDate >= :fromDate")
    void deleteAllBySubscriptionIdFromDate(
        @Param("subId") Long subscriptionId,
        @Param("fromDate") LocalDate fromDate);
}
