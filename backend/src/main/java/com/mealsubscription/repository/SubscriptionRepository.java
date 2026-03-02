package com.mealsubscription.repository;

import com.mealsubscription.entity.Subscription;
import com.mealsubscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Page<Subscription> findByUserId(Long userId, Pageable pageable);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = :status")
    Optional<Subscription> findActiveByUserId(
        @Param("userId") Long userId,
        @Param("status") SubscriptionStatus status);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    long countByStatus(@Param("status") SubscriptionStatus status);

    /** Revenue query: sum of succeeded payments for admin dashboard */
    @Query("SELECT COALESCE(SUM(p.amountCents), 0) FROM Payment p " +
           "WHERE p.status = com.mealsubscription.entity.PaymentStatus.SUCCEEDED " +
           "AND p.paidAt >= :fromDate")
    Long sumSucceededAmountSince(@Param("fromDate") java.time.LocalDateTime fromDate);
}
