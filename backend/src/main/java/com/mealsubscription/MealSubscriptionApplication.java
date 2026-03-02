package com.mealsubscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableAsync
@EnableScheduling
public class MealSubscriptionApplication {
    public static void main(String[] args) {
        SpringApplication.run(MealSubscriptionApplication.class, args);
    }
}
