# Meal Subscription Service

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-grade meal subscription service built with Spring Boot, featuring JWT authentication, Stripe payment integration, and comprehensive E2E testing with Selenium.

## 🌟 Features

- **User Management**: Registration, authentication with JWT tokens, role-based access (USER/ADMIN)
- **Meal Browsing**: Browse available meals with filtering by dietary type (Standard, Vegetarian, Vegan, Gluten-Free, Keto)
- **Subscription Management**: Create, pause, resume, and cancel meal subscriptions (Weekly/Monthly plans)
- **Payment Processing**: Secure payment handling via Stripe with webhook support
- **Admin Dashboard**: Manage meals, view users, and monitor subscriptions
- **RESTful API**: Comprehensive REST API with proper error handling (RFC 9457 ProblemDetail)
- **Responsive UI**: Thymeleaf-based frontend for E2E testing scenarios
- **Database Migrations**: Version-controlled schema management with Flyway
- **Health Monitoring**: Actuator endpoints for application health and metrics

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Testing](#testing)
- [Documentation](#documentation)
- [License](#license)

## 🔧 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** (LTS) - [Download OpenJDK](https://adoptium.net/)
- **Maven 3.8+** - [Download Maven](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** - [Download Docker](https://www.docker.com/get-started)
- **Git** - [Download Git](https://git-scm.com/downloads)

Optional (for local development without Docker):
- **PostgreSQL 16+** - [Download PostgreSQL](https://www.postgresql.org/download/)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/meal-subscription-service.git
cd meal-subscription-service
```

### 2. Set Up Environment Variables

Copy the example environment file and configure your settings:

```bash
cp .env.example .env
```

Edit `.env` with your configuration:

```env
# Stripe keys (use test keys for development)
STRIPE_SECRET_KEY=sk_test_your_key_here
STRIPE_WEBHOOK_SECRET=whsec_your_secret_here

# JWT secret (generate with: openssl rand -base64 64)
JWT_SECRET=YourSecureRandomBase64EncodedStringAtLeast64CharsLong==

# Mail configuration (optional for local dev)
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### 3. Run with Docker Compose (Recommended)

Start the application with PostgreSQL:

```bash
docker-compose up --build
```

The application will be available at:
- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **PostgreSQL**: localhost:5432 (for DB clients)

### 4. Alternative: Run Locally

If you prefer to run without Docker:

```bash
# Start PostgreSQL (ensure it's running on localhost:5432)
# Create database: CREATE DATABASE mealdb;

cd meal-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. Access the Application

**Frontend Pages:**
- Login: http://localhost:8080/login
- Register: http://localhost:8080/register
- Dashboard: http://localhost:8080/dashboard
- Meals: http://localhost:8080/meals
- Admin Panel: http://localhost:8080/admin/meals

**Default Test Users** (seeded in dev mode):
- **Admin**: `admin@mealsubscription.com` / `Admin@1234`
- **User**: `user@mealsubscription.com` / `User@1234`

**API Endpoints:**
- Base URL: http://localhost:8080/api/v1
- Health: http://localhost:8080/actuator/health

## 📁 Project Structure

```
meal-subscription-service/
├── meal-service/                   # Main Spring Boot application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mealsubscription/
│   │   │   │   ├── config/         # Spring configuration classes
│   │   │   │   ├── controller/     # REST Controllers & Frontend controllers
│   │   │   │   ├── dto/            # Data Transfer Objects (Request/Response)
│   │   │   │   ├── entity/         # JPA Entities
│   │   │   │   ├── exception/      # Custom exceptions & global handler
│   │   │   │   ├── repository/     # Spring Data JPA repositories
│   │   │   │   ├── security/       # JWT & Security configuration
│   │   │   │   ├── service/        # Business logic layer
│   │   │   │   └── util/           # Utility classes
│   │   │   └── resources/
│   │   │       ├── application.yml              # Base configuration
│   │   │       ├── application-dev.yml          # Development profile
│   │   │       ├── application-test.yml         # Test profile
│   │   │       ├── db/migration/                # Flyway SQL migrations
│   │   │       ├── static/                      # CSS, JS, images
│   │   │       └── templates/                   # Thymeleaf HTML templates
│   │   └── test/
│   │       └── java/com/mealsubscription/
│   │           ├── e2e/            # Selenium E2E tests
│   │           │   ├── base/       # Base test classes
│   │           │   ├── config/     # WebDriver configuration
│   │           │   ├── pages/      # Page Object Model classes
│   │           │   └── tests/      # E2E test scenarios
│   │           └── service/        # Unit tests for services
│   └── pom.xml                     # Module dependencies
├── docker-compose.yml              # Docker orchestration
├── Dockerfile                      # Multi-stage Docker build
├── pom.xml                         # Parent POM
├── .env.example                    # Environment variables template
└── README.md                       # This file
```

## 🛠️ Technology Stack

### Backend
- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 3.4.3** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Data persistence layer
- **Hibernate** - ORM implementation
- **Flyway** - Database migration management

### Database
- **PostgreSQL 16** - Primary production database (Docker)
- **H2** - In-memory database for testing

### Security
- **JWT (JJWT 0.12.6)** - Token-based authentication (HS512)
- **BCrypt** - Password hashing (cost factor 12)

### Payment
- **Stripe Java SDK 31.4.0** - Payment processing & webhook handling

### Code Quality
- **Lombok 1.18.40** - Boilerplate reduction
- **MapStruct 1.6.3** - DTO-Entity mapping (compile-time)

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Selenium 4.41.0** - Browser automation for E2E tests
- **WebDriverManager 6.3.3** - Automatic WebDriver management

### Frontend
- **Thymeleaf** - Server-side templating
- **HTML5, CSS3, JavaScript** - Frontend technologies

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Maven** - Build automation

## 🔌 API Endpoints

### Authentication (`/api/v1/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | Register new user | No |
| POST | `/login` | Login and get JWT token | No |

### Users (`/api/v1/users`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/me` | Get current user profile | Yes |

### Meals (`/api/v1/meals`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List all available meals | No |
| GET | `/{id}` | Get meal details | No |
| POST | `/` | Create new meal | Yes (Admin) |
| PUT | `/{id}` | Update meal | Yes (Admin) |
| DELETE | `/{id}` | Delete meal | Yes (Admin) |

### Subscriptions (`/api/v1/subscriptions`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Create subscription | Yes |
| GET | `/` | List user's subscriptions | Yes |
| GET | `/{id}` | Get subscription details | Yes |
| POST | `/{id}/pause` | Pause subscription | Yes |
| POST | `/{id}/resume` | Resume subscription | Yes |
| POST | `/{id}/cancel` | Cancel subscription | Yes |

### Payments (`/api/v1/payments`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/webhook` | Stripe webhook handler | No (Verified) |
| GET | `/` | List user's payments | Yes |
| GET | `/all` | List all payments | Yes (Admin) |

### Admin (`/api/v1/admin`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/users` | List all users | Yes (Admin) |
| GET | `/users/{id}` | Get user details | Yes (Admin) |
| DELETE | `/users/{id}` | Deactivate user | Yes (Admin) |
| GET | `/subscriptions` | List all subscriptions | Yes (Admin) |
| GET | `/subscriptions/{id}` | Get subscription details | Yes (Admin) |

**For detailed API documentation with request/response examples, see [API.md](docs/API.md)**

## ⚙️ Configuration

### Application Profiles

- **Default** (no profile): H2 in-memory database, suitable for quick testing
- **dev**: PostgreSQL database, debug logging, Flyway migrations with test data
- **test**: H2 in-memory with PostgreSQL compatibility mode, used by tests

Activate a profile:

```bash
# Via Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Via Java
java -jar -Dspring.profiles.active=dev meal-service.jar

# Via Environment Variable
export SPRING_PROFILES_ACTIVE=dev
```

### Key Configuration Properties

Edit `meal-service/src/main/resources/application.yml` or use environment variables:

```yaml
app:
  jwt:
    secret: ${JWT_SECRET}          # JWT signing key (256+ bits)
    expiration-ms: 86400000        # 24 hours
  
  stripe:
    secret-key: ${STRIPE_SECRET_KEY}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET}
    currency: usd
  
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}
```

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

Unit tests are located in `meal-service/src/test/java/com/mealsubscription/service/`

### Run E2E Tests with Selenium

```bash
# Headless mode (default)
mvn test -Pe2e -pl meal-service

# With visible browser
mvn test -Pe2e -pl meal-service -Dheadless=false

# Against different URL
mvn test -Pe2e -pl meal-service -Dapp.base.url=http://staging.example.com
```

E2E tests require the application to be running. Start it first:

```bash
docker-compose up
```

**For detailed testing documentation, see [TESTING.md](docs/TESTING.md)**

## 📚 Documentation

- **[API Documentation](docs/API.md)** - Complete REST API reference with examples
- **[Testing Guide](docs/TESTING.md)** - How to run and write tests
- **[Deployment Guide](docs/DEPLOYMENT.md)** - Production deployment instructions
- **[Architecture Overview](docs/ARCHITECTURE.md)** - System architecture and design patterns
- **[Contributing Guidelines](docs/CONTRIBUTING.md)** - How to contribute to the project
- **[Stripe Setup](docs/STRIPE_SETUP.md)** - Stripe integration and webhook configuration

## 🛡️ Security

- **Authentication**: JWT-based stateless authentication (HS512 algorithm)
- **Password Storage**: BCrypt hashing with cost factor 12
- **Role-Based Access**: USER and ADMIN roles with method-level security
- **HTTPS Headers**: Content Security Policy, X-Frame-Options, X-Content-Type-Options
- **Stripe Webhooks**: HMAC-SHA256 signature verification
- **SQL Injection Prevention**: Parameterized queries via JPA
- **CORS**: Configurable allowed origins

## 🐛 Troubleshooting

### Application won't start

**Error**: `Error creating bean with name 'jwtTokenProvider'`

**Solution**: Ensure `JWT_SECRET` is set in your `.env` file and is at least 32 characters long.

### Database connection failed

**Error**: `Connection refused: localhost:5432`

**Solution**: 
1. Ensure PostgreSQL is running: `docker-compose up db`
2. Check credentials in `.env` match those in `docker-compose.yml`
3. Verify database exists: `docker exec -it meal-subscription-service-db-1 psql -U meal -d mealdb`

### No static resource error

**Error**: `No static resource .`

**Solution**: This was a known issue with missing root path handler. Ensure you have the latest version with the root redirect in `FrontendController.java`.

### Tests failing

**Solution**: 
1. Ensure application is running for E2E tests
2. Check test database profile is active
3. Clear target directory: `mvn clean`

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](docs/CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## 📧 Contact

For questions or support, please open an issue in the GitHub repository.

---

**Built with ❤️ using Spring Boot**
