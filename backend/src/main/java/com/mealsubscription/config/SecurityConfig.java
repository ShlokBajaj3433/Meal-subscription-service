package com.mealsubscription.config;

import com.mealsubscription.security.JwtAuthenticationFilter;
import com.mealsubscription.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on controllers/services
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // ── CSRF: disabled for stateless JWT REST API ──────────────────
            // Card input is handled by Stripe.js hosted fields — browser never
            // sends raw card data cross-origin, so CSRF on /webhook is irrelevant.
            .csrf(AbstractHttpConfigurer::disable)

            // ── Session: stateless (JWT carried in Authorization header) ───
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── Route authorization ────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/payments/webhook").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/meals/**").permitAll()
                // Admin-only
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // All other endpoints require authentication
                .anyRequest().authenticated())

            // ── JWT filter ─────────────────────────────────────────────────
            .userDetailsService(userDetailsService)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // ── Error responses for unauthenticated / forbidden ────────────
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                .accessDeniedHandler((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")))

            // ── Secure HTTP headers ────────────────────────────────────────
            .headers(headers -> headers
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' https://js.stripe.com; " +
                        "frame-src https://js.stripe.com; " +
                        "frame-ancestors 'none'"))
                .frameOptions(fo -> fo.deny())
                .xssProtection(xss -> xss.disable())      // CSP is the modern mechanism
                .contentTypeOptions(ct -> {})              // X-Content-Type-Options: nosniff
                .referrerPolicy(ref ->
                    ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicy(pp ->
                    pp.policy("camera=(), microphone=(), geolocation=()")))
            .build();
    }

    /**
     * BCrypt with cost factor 12 ≈ 250ms hashing time on modern hardware.
     * Balances security strength against server load during login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
            throws Exception {
        return cfg.getAuthenticationManager();
    }
}
