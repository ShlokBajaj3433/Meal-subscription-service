# Architecture Documentation

Comprehensive architecture overview of the Meal Subscription Service.

## Table of Contents

- [System Overview](#system-overview)
- [Architecture Style](#architecture-style)
- [Layer Architecture](#layer-architecture)
- [Domain Model](#domain-model)
- [Security Architecture](#security-architecture)
- [Database Schema](#database-schema)
- [Payment Integration](#payment-integration)
- [Configuration Management](#configuration-management)
- [Design Patterns](#design-patterns)
- [Technology Decisions](#technology-decisions)

## System Overview

The Meal Subscription Service is a **monolithic Spring Boot application** that provides meal subscription management with payment processing capabilities.

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTS                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  Web Browser │  │  Mobile App  │  │   Stripe     │         │
│  │  (Thymeleaf) │  │  (REST API)  │  │  (Webhooks)  │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    NGINX (Reverse Proxy)                         │
│  - SSL Termination                                               │
│  - Rate Limiting                                                 │
│  - Static Asset Caching                                          │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│              SPRING BOOT APPLICATION (Port 8080)                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │               PRESENTATION LAYER                          │ │
│  │  ┌──────────────────┐  ┌──────────────────────────────┐  │ │
│  │  │ REST Controllers  │  │  Frontend Controllers       │  │ │
│  │  │  /api/v1/*       │  │  (Thymeleaf Templates)      │  │ │
│  │  └──────────────────┘  └──────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │               SECURITY LAYER                              │ │
│  │  - JWT Authentication Filter                              │ │
│  │  - Spring Security Configuration                          │ │
│  │  - Role-Based Access Control                              │ │
│  └───────────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │               SERVICE LAYER (Business Logic)              │ │
│  │  UserService │ MealService │ SubscriptionService │ etc.   │ │
│  └───────────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │               DATA ACCESS LAYER                           │ │
│  │  Spring Data JPA Repositories                             │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   POSTGRESQL DATABASE                            │
│  - Users, Meals, Subscriptions, Payments                        │
│  - Managed by Flyway migrations                                 │
└─────────────────────────────────────────────────────────────────┘
```

### External Dependencies

| Service | Purpose | Protocol |
|---------|---------|----------|
| PostgreSQL | Primary data store | JDBC |
| Stripe API | Payment processing | HTTPS/REST |
| SMTP Server | Email notifications | SMTP |

## Architecture Style

### Monolithic with Layered Architecture

**Why Monolithic?**
- Simpler deployment and operations
- Lower initial complexity
- Sufficient for current scale
- Easy to develop and test locally

**Future Migration Path**: If scale demands, can migrate to microservices:
- User Service
- Meal Catalog Service
- Subscription Service
- Payment Service
- Notification Service

### Key Characteristics

- **Stateless**: No server-side sessions (JWT-based auth)
- **RESTful**: Follows REST principles for API design
- **Data-driven**: Domain model centered around entities
- **Transaction-based**: ACID guarantees for critical operations

## Layer Architecture

### 1. Presentation Layer

**Responsibility**: Handle HTTP requests/responses

#### REST Controllers

```
com/mealsubscription/controller/
├── AuthController.java          # /api/v1/auth
├── UserController.java          # /api/v1/users
├── MealController.java          # /api/v1/meals
├── SubscriptionController.java  # /api/v1/subscriptions
├── PaymentController.java       # /api/v1/payments
└── AdminController.java         # /api/v1/admin
```

**Responsibilities**:
- Request validation (`@Valid`)
- DTO mapping
- HTTP status code selection
- Exception handling delegation

**Example Pattern**:
```java
@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
public class MealController {
    
    private final MealService mealService;

    @GetMapping
    public ResponseEntity<Page<MealResponse>> listMeals(
            @RequestParam(required = false) DietaryType dietary,
            Pageable pageable) {
        
        Page<MealResponse> meals = mealService.listAvailable(dietary, pageable);
        return ResponseEntity.ok(meals);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MealResponse> createMeal(
            @Valid @RequestBody MealRequest request) {
        
        MealResponse created = mealService.createMeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

#### Frontend Controllers

```
FrontendController.java          # Thymeleaf page rendering
```

Serves HTML pages for E2E testing with embedded authentication.

### 2. Service Layer

**Responsibility**: Business logic and orchestration

```
com/mealsubscription/service/
├── UserService.java             # Interface
├── MealService.java
├── SubscriptionService.java
├── PaymentService.java
└── impl/
    ├── UserServiceImpl.java     # Implementation
    ├── MealServiceImpl.java
    ├── SubscriptionServiceImpl.java
    └── PaymentServiceImpl.java
```

**Responsibilities**:
- Business rule enforcement
- Transaction management (`@Transactional`)
- Entity-DTO conversion (via `EntityMapper`)
- Coordination between repositories
- Exception handling (business exceptions)

**Design Principles**:
- **Interface segregation**: Each service has an interface
- **Single responsibility**: Each service manages one domain area
- **Dependency injection**: Constructor-based (via `@RequiredArgsConstructor`)

### 3. Data Access Layer

**Responsibility**: Database operations

```
com/mealsubscription/repository/
├── UserRepository.java
├── MealRepository.java
├── SubscriptionRepository.java
├── SubscriptionMealRepository.java
└── PaymentRepository.java
```

**Pattern**: Spring Data JPA repositories

```java
public interface MealRepository extends JpaRepository<Meal, Long> {
    
    // Derived query methods
    List<Meal> findByDietaryTypeAndIsAvailableTrue(DietaryType type);
    
    boolean existsByName(String name);
    
    // Custom queries
    @Query("SELECT m FROM Meal m WHERE m.calories BETWEEN :min AND :max")
    List<Meal> findByCalorieRange(
        @Param("min") int min, 
        @Param("max") int max
    );
}
```

### Cross-Cutting Concerns

#### Exception Handling

**Global Exception Handler**:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, 
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }
    
    // Other exception handlers...
}
```

Follows **RFC 9457 Problem Details** standard.

#### Validation

Bean Validation (JSR-380) annotations:

```java
public record MealRequest(
    @NotBlank @Size(min = 3, max = 100) String name,
    @NotBlank @Size(min = 10, max = 500) String description,
    @NotNull DietaryType dietaryType,
    @Positive @Max(5000) Integer calories,
    @Positive Integer priceCents
) {}
```

#### Logging

SLF4J with Logback:

```java
@Slf4j
public class MealServiceImpl implements MealService {
    
    public MealResponse createMeal(MealRequest request) {
        log.debug("Creating meal: {}", request.name());
        // Business logic
        log.info("Meal created successfully: id={}", meal.getId());
        return response;
    }
}
```

## Domain Model

### Core Entities

#### User

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private Role role;  // USER, ADMIN
    
    private boolean isActive;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### Meal

```java
@Entity
@Table(name = "meals")
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    private DietaryType dietaryType;
    
    private Integer calories;
    
    private Integer priceCents;  // Store as cents to avoid floating-point issues
    
    private String imageUrl;
    
    private boolean isAvailable;
}
```

#### Subscription

```java
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private PlanType planType;  // WEEKLY, MONTHLY
    
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;  // ACTIVE, PAUSED, CANCELLED, EXPIRED
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @Column(unique = true)
    private String stripeSubscriptionId;
    
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<SubscriptionMeal> meals;
}
```

#### Payment

```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
    
    @Column(unique = true)
    private String stripePaymentIntentId;
    
    private String stripeInvoiceId;
    
    private String stripeCustomerId;
    
    private Integer amountCents;
    
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;  // PENDING, SUCCEEDED, FAILED, REFUNDED
    
    private LocalDateTime paidAt;
}
```

### Entity Relationships

```
┌─────────┐         ┌──────────────┐         ┌──────┐
│  User   │ 1     * │ Subscription │ *     * │ Meal │
│         ├─────────┤              ├─────────┤      │
│         │         │              │         │      │
└─────────┘         └──────┬───────┘         └──────┘
                           │
                           │ 1
                           │
                           │ *
                    ┌──────┴────────┐
                    │   Payment     │
                    │               │
                    └───────────────┘
```

### DTO Pattern

**Request DTOs**: For incoming data (immutable records)

```java
public record MealRequest(
    String name,
    String description,
    DietaryType dietaryType,
    Integer calories,
    Integer priceCents,
    String imageUrl,
    Boolean isAvailable
) {}
```

**Response DTOs**: For outgoing data

```java
public record MealResponse(
    Long id,
    String name,
    String description,
    DietaryType dietaryType,
    Integer calories,
    Integer priceCents,
    String imageUrl,
    Boolean isAvailable
) {}
```

**Mapping**: MapStruct for DTO ↔ Entity conversion

```java
@Mapper(componentModel = "spring")
public interface EntityMapper {
    MealResponse toResponse(Meal meal);
    Meal toEntity(MealRequest request);
    List<MealResponse> toResponseList(List<Meal> meals);
}
```

## Security Architecture

### Authentication Flow

```
1. User → POST /api/v1/auth/login {email, password}
2. AuthController → AuthenticationManager.authenticate()
3. UserDetailsService → Load user from database
4. PasswordEncoder → Verify BCrypt hash
5. JwtTokenProvider → Generate JWT (HS512)
6. Response → {token, user details}

Subsequent requests:
1. Client → Include "Authorization: Bearer {token}"
2. JwtAuthenticationFilter → Extract and validate token
3. If valid → Set SecurityContext with Authentication
4. Controller → Access via @AuthenticationPrincipal
```

### JWT Token Structure

**Header**:
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

**Payload**:
```json
{
  "sub": "user@example.com",
  "roles": "ROLE_USER",
  "iat": 1709379000,
  "exp": 1709465400
}
```

**Signature**: HMAC-SHA512 with secret key

### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())  // Stateless JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### Authorization

**Role-Based Access Control (RBAC)**:

- **USER**: Can manage own subscriptions, view meals
- **ADMIN**: Full access to all resources

**Method-level security**:

```java
@PreAuthorize("hasRole('ADMIN')")
public MealResponse createMeal(MealRequest request) {
    // Only admins can create meals
}

@PreAuthorize("hasRole('USER')")
public List<SubscriptionResponse> getUserSubscriptions() {
    // Only authenticated users
}
```

### Password Security

- **Algorithm**: BCrypt
- **Cost Factor**: 12 (~250ms on modern hardware)
- **Salt**: Automatically generated per password

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

## Database Schema

### Schema Overview

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Meals table
CREATE TABLE meals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    dietary_type VARCHAR(20) NOT NULL,
    calories INTEGER NOT NULL,
    price_cents INTEGER NOT NULL,
    image_url VARCHAR(500),
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Subscriptions table
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    plan_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    stripe_subscription_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Subscription-Meal junction table
CREATE TABLE subscription_meals (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id),
    meal_id BIGINT NOT NULL REFERENCES meals(id),
    delivery_date DATE NOT NULL,
    quantity INTEGER NOT NULL,
    UNIQUE(subscription_id, meal_id, delivery_date)
);

-- Payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id),
    stripe_payment_intent_id VARCHAR(255) UNIQUE NOT NULL,
    stripe_invoice_id VARCHAR(255),
    stripe_customer_id VARCHAR(255),
    amount_cents INTEGER NOT NULL,
    currency VARCHAR(3) DEFAULT 'usd',
    status VARCHAR(20) NOT NULL,
    failure_message TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Indexes

```sql
-- Performance indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_meals_dietary_type ON meals(dietary_type);
CREATE INDEX idx_meals_available ON meals(is_available);
CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_payments_subscription_id ON payments(subscription_id);
CREATE INDEX idx_payments_stripe_payment_intent ON payments(stripe_payment_intent_id);
```

### Migration Strategy

**Flyway** manages schema versions:

```
db/migration/
├── V1__create_users.sql
├── V2__create_meals.sql
├── V3__create_subscriptions.sql
├── V4__create_payments.sql
└── R__seed_dev_data.sql  (Repeatable, dev only)
```

**Naming Convention**: `V{version}__{description}.sql`

## Payment Integration

### Stripe Architecture

```
┌─────────────┐                          ┌─────────────┐
│   Client    │                          │   Stripe    │
└──────┬──────┘                          └──────┬──────┘
       │                                        │
       │ 1. Create Payment Intent               │
       ├────────────────────────────────────────>
       │                                        │
       │ 2. Return client_secret                │
       <────────────────────────────────────────┤
       │                                        │
       │ 3. Confirm payment (card details)      │
       ├────────────────────────────────────────>
       │                                        │
       │ 4. Process payment                     │
       │                                     ┌──▼──┐
       │                                     │     │
       │                                     └──┬──┘
       │ 5. Webhook: payment_intent.succeeded  │
       <────────────────────────────────────────┤
       │                                        │
┌──────▼──────┐                                 │
│  Backend    │                                 │
│  - Verify   │                                 │
│  - Update DB│                                 │
└─────────────┘                                 │
```

### Webhook Security

**Signature Verification**:

```java
@PostMapping("/webhook")
public ResponseEntity<Map<String, Boolean>> handleWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader) {
    
    try {
        // Verify signature
        Event event = Webhook.constructEvent(
            payload, 
            sigHeader, 
            webhookSecret
        );
        
        // Process event
        processStripeEvent(event);
        
        return ResponseEntity.ok(Map.of("received", true));
    } catch (SignatureVerificationException e) {
        log.error("Invalid Stripe signature");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
```

### Idempotency

Payment records use unique constraint on `stripe_payment_intent_id` to prevent duplicate processing.

## Configuration Management

### Profile-Based Configuration

| Profile | Purpose | Database | Features |
|---------|---------|----------|----------|
| **default** | Fallback | H2 in-memory | Quick local testing |
| **dev** | Development | PostgreSQL | Debug logging, test data |
| **test** | Testing | H2 (PostgreSQL mode) | Fast tests |
| **prod** | Production | PostgreSQL | Optimized, no test data |

### Environment-Specific Settings

**Development (`application-dev.yml`)**:
- SQL logging enabled
- Flyway with test data
- Relaxed security for debugging

**Production (`application-prod.yml`)**:
- SQL logging disabled
- Strict security
- Connection pool tuning
- Production SMTP

## Design Patterns

### Patterns Used

| Pattern | Usage | Example |
|---------|-------|---------|
| **Dependency Injection** | All layers | `@RequiredArgsConstructor` |
| **Repository Pattern** | Data access | Spring Data JPA |
| **DTO Pattern** | Data transfer | Request/Response records |
| **Builder Pattern** | Object construction | `@Builder` on DTOs |
| **Strategy Pattern** | Payment processing | Stripe integration |
| **Template Method** | Testing | `BaseTest` for E2E tests |
| **Page Object Model** | E2E testing | Selenium page classes |
| **Singleton** | Spring beans | All `@Service`, `@Repository` |

## Technology Decisions

### Why Java 21?

- Latest LTS with modern features
- Virtual threads (for future use)
- Pattern matching enhancements
- Strong ecosystem

### Why Spring Boot 3?

- Industry standard for Java web applications
- Rich ecosystem of integrations
- Auto-configuration reduces boilerplate
- Production-ready features (Actuator)

### Why PostgreSQL?

- ACID compliance
- Rich data types (JSON, arrays)
- Mature and reliable
- Excellent performance

### Why JWT over Sessions?

- Stateless (scales horizontally)
- Works across domains
- Mobile-friendly
- No server-side storage

### Why MapStruct over Manual Mapping?

- Compile-time code generation (type-safe)
- No reflection overhead
- Reduces boilerplate
- Easy to debug (generated code visible)

### Why Flyway?

- Version control for database
- Reproducible migrations
- Team collaboration
- Rollback capabilities

---

**For implementation details, see source code in respective packages.**
