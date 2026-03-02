package com.mealsubscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "subscription_meals",
    uniqueConstraints = @UniqueConstraint(columnNames = {"subscription_id", "meal_id", "delivery_date"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(nullable = false)
    @Builder.Default
    private int quantity = 1;
}
