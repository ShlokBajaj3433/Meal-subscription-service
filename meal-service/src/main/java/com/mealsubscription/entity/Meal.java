package com.mealsubscription.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_type", nullable = false, length = 50)
    private DietaryType dietaryType;

    @Column
    private Integer calories;

    /**
     * Price stored in minor currency units (cents) to avoid floating-point rounding.
     * e.g. $12.99 is stored as 1299.
     */
    @Column(name = "price_cents", nullable = false)
    private Long priceCents;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private boolean available = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "meal")
    @Builder.Default
    private List<SubscriptionMeal> subscriptionMeals = new ArrayList<>();
}
