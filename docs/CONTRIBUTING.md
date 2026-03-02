# Contributing to Meal Subscription Service

Thank you for your interest in contributing! This guide will help you get started.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of experience level, gender, gender identity and expression, sexual orientation, disability, personal appearance, body size, race, ethnicity, age, religion, or nationality.

### Our Standards

**Examples of positive behavior:**

- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards other community members

**Unacceptable behavior:**

- Trolling, insulting/derogatory comments, and personal or political attacks
- Public or private harassment
- Publishing others' private information without explicit permission
- Other conduct which could reasonably be considered inappropriate in a professional setting

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- **Java 21** installed ([Adoptium JDK](https://adoptium.net/))
- **Maven 3.8+** installed
- **Docker & Docker Compose** installed
- **Git** configured with your identity
- IDE with Java support (IntelliJ IDEA, Eclipse, VS Code)

### Fork and Clone

1. **Fork** the repository on GitHub
2. **Clone** your fork:

```bash
git clone https://github.com/YOUR_USERNAME/meal-subscription-service.git
cd meal-subscription-service
```

3. **Add upstream** remote:

```bash
git remote add upstream https://github.com/ORIGINAL_OWNER/meal-subscription-service.git
```

### Set Up Development Environment

1. **Copy environment file**:

```bash
cp .env.example .env
# Edit .env with your local configuration
```

2. **Start dependencies**:

```bash
docker-compose up db  # Start only PostgreSQL
```

3. **Run the application**:

```bash
cd meal-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. **Verify setup**:

```bash
curl http://localhost:8080/actuator/health
```

### IDE Configuration

#### IntelliJ IDEA

1. **Import as Maven project**
2. **Enable annotation processing**:
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"
3. **Install Lombok plugin**:
   - Settings → Plugins → Search "Lombok" → Install
4. **Code style**:
   - Import code style from `.editorconfig` (auto-detected)

#### Eclipse

1. **Import Maven project**: File → Import → Existing Maven Projects
2. **Install Lombok**: Download `lombok.jar`, run `java -jar lombok.jar`, select Eclipse installation
3. **Enable annotation processing**: Project → Properties → Java Compiler → Annotation Processing

#### VS Code

```bash
# Install extensions
code --install-extension vscjava.vscode-java-pack
code --install-extension GabrielBB.vscode-lombok
```

## Development Workflow

### Branch Strategy

We follow **Git Flow**:

- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Urgent production fixes
- `release/*` - Release preparation

### Creating a Feature Branch

```bash
# Update your fork
git checkout develop
git pull upstream develop

# Create feature branch
git checkout -b feature/your-feature-name

# Work on your feature
# ... make changes ...

# Keep your branch updated
git fetch upstream
git rebase upstream/develop
```

### Branch Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/short-description` | `feature/add-meal-categories` |
| Bug Fix | `bugfix/issue-number-description` | `bugfix/123-fix-login-error` |
| Hotfix | `hotfix/critical-issue` | `hotfix/security-vulnerability` |
| Documentation | `docs/what-changed` | `docs/update-api-examples` |
| Refactor | `refactor/component-name` | `refactor/payment-service` |

## Coding Standards

### Java Code Style

We follow **Google Java Style Guide** with minor modifications.

#### Key Principles

- **Indentation**: 4 spaces (not tabs)
- **Line length**: Max 120 characters
- **Braces**: K&R style (opening brace on same line)
- **Naming**:
  - Classes: `PascalCase`
  - Methods/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase`

#### Example

```java
package com.mealsubscription.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the MealService interface.
 * Handles meal-related business logic.
 */
@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    
    private final MealRepository mealRepository;
    private final EntityMapper entityMapper;

    @Override
    public MealResponse createMeal(MealRequest request) {
        // Validate input
        validateMealRequest(request);
        
        // Create entity
        Meal meal = entityMapper.toEntity(request);
        
        // Persist and return
        Meal saved = mealRepository.save(meal);
        return entityMapper.toResponse(saved);
    }

    private void validateMealRequest(MealRequest request) {
        // Validation logic
    }
}
```

### Lombok Usage

Use Lombok to reduce boilerplate:

✅ **Use**:
- `@RequiredArgsConstructor` for dependency injection
- `@Getter` / `@Setter` for simple DTOs
- `@Builder` for complex object construction
- `@Slf4j` for logging

❌ **Avoid**:
- `@Data` (too implicit)
- `@AllArgsConstructor` (use `@Builder` instead)
- `@ToString` on entities (can cause lazy-loading issues)

### Spring Annotations

```java
// Service layer
@Service
@RequiredArgsConstructor
@Slf4j
public class YourService { }

// Controller layer
@RestController
@RequestMapping("/api/v1/resource")
@RequiredArgsConstructor
public class YourController { }

// Repository layer
@Repository
public interface YourRepository extends JpaRepository<Entity, Long> { }

// Configuration
@Configuration
public class YourConfig { }
```

### Exception Handling

- **Create custom exceptions** for business logic errors
- **Throw meaningful exceptions** with descriptive messages
- **Don't catch generic Exception** unless necessary

```java
// Good
throw new ResourceNotFoundException("Meal with id " + id + " not found");

// Bad
throw new Exception("Error");
```

### Validation

Use Bean Validation annotations:

```java
public record MealRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100)
    String name,
    
    @NotNull
    @Positive
    Integer calories,
    
    @Email
    String contactEmail
) {}
```

### Database Access

- **Use JPA repositories** (no raw JDBC)
- **Use derived query methods** when possible
- **Use `@Query` for complex queries**
- **Always use parameterized queries** (prevents SQL injection)

```java
public interface MealRepository extends JpaRepository<Meal, Long> {
    
    // Derived query method
    List<Meal> findByDietaryTypeAndIsAvailableTrue(DietaryType type);
    
    // Custom query
    @Query("SELECT m FROM Meal m WHERE m.calories < :maxCalories")
    List<Meal> findLowCalorieMeals(@Param("maxCalories") int maxCalories);
}
```

### Logging

Use **SLF4J** with appropriate log levels:

```java
@Slf4j
public class YourService {
    
    public void doSomething() {
        log.debug("Starting operation with param: {}", param);  // Debug details
        
        try {
            // Business logic
            log.info("Operation completed successfully");  // Important milestones
        } catch (Exception e) {
            log.error("Operation failed: {}", e.getMessage(), e);  // Errors with stack trace
        }
    }
}
```

**Log Levels**:
- `ERROR`: Errors requiring immediate attention
- `WARN`: Potential issues (degraded functionality)
- `INFO`: Important business events
- `DEBUG`: Detailed debugging information
- `TRACE`: Very detailed debugging (rarely used)

### Security Best Practices

- ✅ **Never log sensitive data** (passwords, tokens, PII)
- ✅ **Validate all inputs** (use `@Valid` on controller methods)
- ✅ **Use `@PreAuthorize` for authorization checks**
- ✅ **Sanitize user-generated content**
- ❌ **Never commit secrets to version control**
- ❌ **Never use `md5` or `sha1` for passwords** (use BCrypt)

## Testing Guidelines

### Coverage Requirements

- **New features**: 80%+ test coverage
- **Bug fixes**: Add test case that fails without the fix
- **Refactoring**: Maintain existing coverage

### Unit Test Template

```java
@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealRepository mealRepository;
    
    @InjectMocks
    private MealServiceImpl mealService;

    @Test
    void testCreateMeal_Success() {
        // Given
        MealRequest request = MealRequest.builder()
            .name("Test Meal")
            .calories(500)
            .build();
        
        when(mealRepository.save(any(Meal.class)))
            .thenReturn(createMockMeal());
        
        // When
        MealResponse response = mealService.createMeal(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Test Meal");
        verify(mealRepository).save(any(Meal.class));
    }
}
```

### E2E Test Guidelines

- Place in `e2e/tests/` package
- Use Page Object Model pattern
- Ensure tests are **idempotent** (can run multiple times)
- Clean up test data after execution

### Running Tests Before Committing

```bash
# Run all unit tests
mvn test

# Run specific test
mvn test -Dtest=MealServiceTest

# Run with coverage
mvn clean test jacoco:report
```

## Commit Messages

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code formatting (no functionality change)
- `refactor`: Code restructuring (no functionality change)
- `test`: Adding or updating tests
- `chore`: Build process, dependencies, tooling

### Examples

```bash
# Good commit messages
feat(meals): add filtering by dietary type
fix(auth): resolve JWT token expiration bug
docs(api): update endpoint documentation with examples
test(subscription): add unit tests for pause/resume flow
refactor(payment): extract Stripe logic to separate service

# Bad commit messages
fixed stuff
WIP
update
asdfasdf
```

### Scope Guidelines

- `auth`: Authentication/authorization
- `meals`: Meal management
- `subscription`: Subscription handling
- `payment`: Payment processing
- `admin`: Admin functionality
- `db`: Database migrations
- `config`: Configuration changes
- `deps`: Dependency updates

## Pull Request Process

### Before Creating a PR

1. **Ensure tests pass**: `mvn clean test`
2. **Check code style**: Verify formatting matches project standards
3. **Update documentation**: If adding features, update relevant docs
4. **Rebase on develop**: `git rebase upstream/develop`
5. **Squash WIP commits**: Clean up commit history

### Creating a Pull Request

1. **Push your branch**:

```bash
git push origin feature/your-feature-name
```

2. **Create PR on GitHub** with template:

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Checklist
- [ ] Tests pass locally
- [ ] Added unit tests
- [ ] Updated documentation
- [ ] No merge conflicts
- [ ] Followed code style guidelines

## Related Issues
Closes #123
```

3. **Request review** from maintainers

### PR Review Process

- **Reviewers will check**:
  - Code quality and style
  - Test coverage
  - Documentation completeness
  - Security implications
  
- **Address feedback**: Make changes in new commits, don't force-push
- **CI must pass**: All automated checks must succeed

### Merging

- Maintainers will merge approved PRs
- Use **Squash and Merge** for feature branches
- Use **Merge Commit** for release branches

## Issue Reporting

### Bug Reports

Use the bug report template:

```markdown
**Describe the bug**
Clear description of the issue

**To Reproduce**
1. Go to '...'
2. Click on '...'
3. See error

**Expected behavior**
What should happen

**Screenshots**
If applicable

**Environment:**
- OS: [e.g., Ubuntu 22.04]
- Java version: [e.g., 21]
- Browser: [e.g., Chrome 120]

**Additional context**
Any other relevant information
```

### Feature Requests

```markdown
**Is your feature request related to a problem?**
Description of the problem

**Describe the solution you'd like**
Clear description of desired functionality

**Describe alternatives you've considered**
Other approaches you've thought about

**Additional context**
Any other relevant information
```

## Documentation

- **Update README.md** for user-facing changes
- **Update API.md** for new/modified endpoints
- **Update ARCHITECTURE.md** for structural changes
- **Add inline comments** for complex logic
- **Write JavaDoc** for public APIs

## Questions?

- **GitHub Discussions**: For general questions
- **GitHub Issues**: For bugs and feature requests
- **Email**: maintainers@example.com (for security issues)

## Recognition

Contributors will be acknowledged in:
- CONTRIBUTORS.md file
- Release notes
- GitHub contributors page

Thank you for contributing! 🎉
