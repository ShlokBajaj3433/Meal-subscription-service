package com.mealsubscription.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StripeConfig {

    @Value("${app.stripe.secret-key}")
    private String stripeSecretKey;

    /**
     * Initialises the Stripe SDK global API key on application startup.
     * The key is read from the environment variable STRIPE_SECRET_KEY —
     * never hard-coded in properties files committed to version control.
     */
    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe SDK initialised (key prefix: {})",
            stripeSecretKey.substring(0, Math.min(7, stripeSecretKey.length())));
    }
}
