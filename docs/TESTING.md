# Testing Guide

Comprehensive guide for running and writing tests in the Meal Subscription Service.

## Table of Contents

- [Overview](#overview)
- [Testing Strategy](#testing-strategy)
- [Running Tests](#running-tests)
- [Unit Tests](#unit-tests)
- [End-to-End Tests](#end-to-end-tests)
- [Test Data](#test-data)
- [Writing New Tests](#writing-new-tests)
- [CI/CD Integration](#cicd-integration)
- [Troubleshooting](#troubleshooting)

## Overview

The project uses a multi-layered testing approach:

- **Unit Tests**: Test individual components in isolation (services, utilities)
- **Integration Tests**: Test component interactions with real database
- **E2E Tests**: Test complete user workflows with Selenium WebDriver

### Test Frameworks

- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework for unit tests
- **AssertJ**: Fluent assertions
- **Spring Boot Test**: Spring context for integration tests
- **Selenium WebDriver 4**: Browser automation
- **WebDriverManager**: Automatic driver management

## Testing Strategy

### Test Pyramid

```
           /\
          /  \    E2E Tests (Selenium)
         /    \   - Critical user flows
        /------\  - Admin workflows
       /        \
      / Integration Tests
     /  - Service layer with DB
    /    - Repository layer
   /      - Security configuration
  /________________________\
       Unit Tests
  - Service business logic
  - Utility functions
  - Validation logic
```

### Coverage Goals

- **Unit Tests**: 80%+ coverage for service layer
- **Integration Tests**: Critical paths (authentication, payments)
- **E2E Tests**: Key user journeys (registration → login → subscribe)

## Running Tests

### Quick Start

```bash
# Run all unit tests (excludes E2E)
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run specific test method
mvn test -Dtest=UserServiceTest#testRegisterUser_Success
```

### Run All Tests (Including E2E)

```bash
# 1. Start the application
docker-compose up

# 2. In another terminal, run E2E tests
mvn test -Pe2e -pl meal-service
```

### Test Profiles

| Command | Profile | Database | Tests Run |
|---------|---------|----------|-----------|
| `mvn test` | test | H2 in-memory | Unit + Integration |
| `mvn test -Pe2e` | test | H2 in-memory | E2E only |
| `mvn test -Dspring.profiles.active=dev` | dev | PostgreSQL | Unit + Integration |

## Unit Tests

### Location

```
meal-service/src/test/java/com/mealsubscription/service/
├── UserServiceTest.java
├── MealServiceTest.java
├── SubscriptionServiceTest.java
└── PaymentServiceTest.java
```

### Example: UserServiceTest

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private EntityMapper entityMapper;
    
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testRegisterUser_Success() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "John Doe", "john@example.com", "password123"
        );
        
        when(userRepository.existsByEmail(request.email()))
            .thenReturn(false);
        when(passwordEncoder.encode(request.password()))
            .thenReturn("hashedPassword");
        
        // When
        UserResponse response = userService.register(request);
        
        // Then
        assertThat(response.email()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "John Doe", "john@example.com", "password123"
        );
        
        when(userRepository.existsByEmail(request.email()))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> userService.register(request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("already exists");
    }
}
```

### Running Unit Tests

```bash
# All unit tests
mvn test

# Specific package
mvn test -Dtest="com.mealsubscription.service.*Test"

# With coverage report
mvn test jacoco:report
# View: target/site/jacoco/index.html
```

## End-to-End Tests

### Prerequisites

> **Important:** the application now includes a built-in data seeder that
> creates two users (admin/user) and several sample meals on startup when the
> database is empty. This ensures the Selenium tests have the credentials and
> data they expect. Simply start the app; no manual SQL is required.

1. **Application must be running** (H2 or PostgreSQL):
   ```bash
   # start locally in dev mode
   mvn spring-boot:run
   # or with Docker compose
   docker-compose up
   ```

2. **Browser installed**
   - Chrome or **Brave (Chromium)**: the driver is managed automatically.
   - You can also use Edge by setting `-Dbrowser=edge`.

3. (optional) **Check app health**
   ```bash
   curl http://localhost:8080/actuator/health  # should return {"status":"UP"}
   ```

### Location

```
meal-service/src/test/java/com/mealsubscription/e2e/
├── base/
│   └── BaseTest.java              # WebDriver lifecycle
├── config/
│   └── DriverFactory.java         # WebDriver configuration
├── pages/                          # Page Object Model
│   ├── LoginPage.java
│   ├── RegisterPage.java
│   ├── DashboardPage.java
│   ├── MealsPage.java
│   └── AdminMealPage.java
└── tests/                          # Test scenarios
    ├── AuthFlowTest.java
    ├── MealSelectionTest.java
    └── AdminMealTest.java
```

### Running E2E Tests

Because the `DriverFactory` now auto-detects Chrome, Brave (Chromium) or Edge,
you can specify the target browser at runtime. Use `-Dbrowser=chromium` to
force Brave/Chrome if you have a Chromium-based browser but no official Chrome
binary.

```bash
# default headless (CI)
mvn test -Pe2e -pl meal-service

# visible Chromium window
mvn test -Pe2e -pl meal-service -Dbrowser=chromium -Dheadless=false

# run against a different host
mvn test -Pe2e -pl meal-service -Dapp.base.url=http://staging.example.com

# run a single E2E class
mvn test -Pe2e -pl meal-service -Dtest=AuthFlowTest
```

```bash
# Headless mode (default - faster, for CI/CD)
mvn test -Pe2e -pl meal-service

# Visible browser (for debugging)
mvn test -Pe2e -pl meal-service -Dheadless=false

# Against different URL
mvn test -Pe2e -pl meal-service -Dapp.base.url=http://staging.example.com

# Specific E2E test
mvn test -Pe2e -pl meal-service -Dtest=AuthFlowTest
```

### E2E Test Configuration

System properties:

- `app.base.url`: Application URL (default: `http://localhost:8080`)
- `headless`: Run in headless mode (default: `true`)
- `webdriver.chrome.driver`: Path to ChromeDriver (optional, auto-managed)

### Page Object Model Pattern

**Example: LoginPage.java**

```java
public class LoginPage {
    private final WebDriver driver;
    
    @FindBy(id = "email")
    private WebElement emailField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;
    
    @FindBy(css = ".error-message")
    private WebElement errorMessage;
    
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public void login(String email, String password) {
        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
    }
    
    public boolean isErrorDisplayed() {
        return errorMessage.isDisplayed();
    }
    
    public String getErrorMessage() {
        return errorMessage.getText();
    }
}
```

**Example Test:**

```java
@Test
void testLoginWithValidCredentials() {
    // Navigate to login page
    driver.get(baseUrl + "/login");
    LoginPage loginPage = new LoginPage(driver);
    
    // Perform login
    loginPage.login("user@mealsubscription.com", "User@1234");
    
    // Verify redirect to dashboard
    wait.until(ExpectedConditions.urlContains("/dashboard"));
    assertThat(driver.getCurrentUrl()).endsWith("/dashboard");
}
```

### E2E Test Scenarios

#### AuthFlowTest

- ✅ Register new user
- ✅ Login with valid credentials
- ✅ Login with invalid credentials (error display)
- ✅ Logout functionality

#### MealSelectionTest

- ✅ Browse all meals
- ✅ Filter meals by dietary type
- ✅ View meal details
- ✅ Add meals to cart

#### AdminMealTest

- ✅ Admin login
- ✅ Create new meal
- ✅ Update existing meal
- ✅ Delete meal
- ✅ Toggle meal availability

## Test Data

### Seeded Test Users

Test data is automatically seeded in **dev** profile from `R__seed_dev_data.sql`:

| Email | Password | Role | Description |
|-------|----------|------|-------------|
| `admin@mealsubscription.com` | `Admin@1234` | ADMIN | Full admin access |
| `user@mealsubscription.com` | `User@1234` | USER | Regular user account |

### Seeded Test Meals

8 sample meals across all dietary types:

1. Grilled Chicken Salad (STANDARD) - $12.99
2. Beef Burger (STANDARD) - $14.99
3. Margherita Pizza (VEGETARIAN) - $11.99
4. Greek Salad (VEGETARIAN) - $10.99
5. Quinoa Buddha Bowl (VEGAN) - $11.99
6. Chickpea Curry (VEGAN) - $10.99
7. Gluten-Free Pasta (GLUTEN_FREE) - $13.99
8. Keto Bowl (KETO) - $15.99

### Using Test Data

```java
@Test
void testLoginWithSeededUser() {
    loginPage.login("user@mealsubscription.com", "User@1234");
    // User should be logged in successfully
}
```

### Resetting Test Data

**Development:**
```bash
# Stop containers
docker-compose down

# Remove volumes (clears database)
docker-compose down -v

# Restart (re-seeds data)
docker-compose up
```

**Testing:**
```bash
# Test profile uses H2 in-memory, resets automatically
mvn test
```

## Writing New Tests

### Unit Test Template

```java
@ExtendWith(MockitoExtension.class)
class YourServiceTest {

    @Mock
    private YourRepository repository;
    
    @InjectMocks
    private YourServiceImpl service;

    @Test
    void testMethodName_SuccessCondition() {
        // Given (Arrange)
        // Set up test data and mocks
        
        // When (Act)
        // Call the method under test
        
        // Then (Assert)
        // Verify the results
        assertThat(result).isNotNull();
        verify(repository).save(any());
    }

    @Test
    void testMethodName_FailureCondition() {
        // Given
        when(repository.findById(anyLong()))
            .thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> service.methodCall())
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
```

### E2E Test Template

```java
public class YourFeatureTest extends BaseTest {

    private YourPage page;

    @BeforeEach
    void setUp() {
        page = new YourPage(driver);
    }

    @Test
    void testFeature_SuccessScenario() {
        // Navigate
        driver.get(baseUrl + "/your-page");
        
        // Interact
        page.performAction();
        
        // Verify
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("success-message")
        ));
        assertThat(page.getSuccessMessage())
            .contains("Expected text");
    }
}
```

### Best Practices

#### Unit Tests

✅ **Do:**
- Test one thing per test method
- Use descriptive test names: `testMethodName_Condition_ExpectedResult`
- Mock external dependencies
- Use AssertJ for fluent assertions
- Test both success and failure paths

❌ **Don't:**
- Test Spring framework functionality
- Use real database in unit tests
- Make external API calls
- Test private methods directly

#### E2E Tests

✅ **Do:**
- Use Page Object Model pattern
- Use explicit waits (WebDriverWait)
- Test critical user journeys
- Clean up test data
- Make tests idempotent

❌ **Don't:**
- Use Thread.sleep() (use waits instead)
- Hard-code test data in tests
- Test every edge case (use unit tests)
- Depend on test execution order

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Run unit tests
        run: mvn test
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3

  e2e-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Start application
        run: docker-compose up -d
      
      - name: Wait for app
        run: |
          timeout 60 bash -c 'until curl -f http://localhost:8080/actuator/health; do sleep 2; done'
      
      - name: Run E2E tests
        run: mvn test -Pe2e -pl meal-service -Dheadless=true
      
      - name: Stop application
        if: always()
        run: docker-compose down
```

## Troubleshooting

### Common Issues

#### 1. E2E Tests Fail: "Connection refused"

**Problem**: Application not running

**Solution**:
```bash
# Verify app is running
curl http://localhost:8080/actuator/health

# If not, start it
docker-compose up
```

#### 2. ChromeDriver/Browser Mismatch

**Problem**: WebDriverManager downloads an incompatible driver version or the
local Chromium variant (Brave) isn't picked up.

**Solution**:
```bash
# clear cached drivers and let WDM pick the right one again
rm -rf ~/.cache/selenium
# explicitly force selection
mvn test -Pe2e -Dbrowser=chromium -Dheadless=false
```
**Problem**: WebDriverManager downloaded incompatible version

**Solution**:
```bash
# Clear WebDriverManager cache
rm -rf ~/.cache/selenium

# Or specify version
mvn test -Pe2e -Dwdm.chromeDriverVersion=120.0.6099.109
```

#### 3. Tests Pass Locally, Fail in CI

**Problem**: Timing issues in headless mode

**Solution**: Increase wait timeouts in BaseTest:

```java
wait = new WebDriverWait(driver, Duration.ofSeconds(30));  // Increase from 15
```

#### 4. Database State Conflicts

**Problem**: Tests interfere with each other

**Solution**:
```bash
# Use @Transactional in integration tests
@SpringBootTest
@Transactional  // Rolls back after each test
class YourIntegrationTest {
    // Tests
}
```

#### 5. Maven Surefire Out of Memory

**Problem**: Large test suite causes OOM

**Solution**: Increase memory in `pom.xml`:

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-Xmx1024m</argLine>
    </configuration>
</plugin>
```

### Debug Mode

#### Unit Tests

```bash
# Run with debug output
mvn test -X

# Attach debugger (port 5005)
mvn test -Dmaven.surefire.debug
```

#### E2E Tests

```bash
# Visible browser + pauses
mvn test -Pe2e -Dheadless=false

# Take screenshots on failure (add to BaseTest)
@AfterEach
void tearDown(TestInfo testInfo) {
    if (testInfo.getTags().contains("failed")) {
        File screenshot = ((TakesScreenshot) driver)
            .getScreenshotAs(OutputType.FILE);
        // Save screenshot
    }
    driver.quit();
}
```

### Generating Test Reports

#### Surefire Reports

Automatically generated at `target/surefire-reports/`

```bash
mvn test
ls target/surefire-reports/
# TEST-*.xml files for CI integration
```

#### Coverage Reports (JaCoCo)

```bash
# Add to pom.xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>

# Generate report
mvn clean test jacoco:report

# View: target/site/jacoco/index.html
```


### Data Seeder

A lightweight `ApplicationRunner` (`DataSeeder.java`) seeds two accounts and a
set of sample meals every time the application starts. This ensures the
Selenium tests have known credentials and stock data without manual setup. The
accounts are:

- `admin@mealsubscription.com` / `Admin@1234` (ADMIN role)
- `user@mealsubscription.com` / `User@1234` (USER role)

If you modify the seeder logic, remember to update the E2E test constants
(cls `AuthFlowTest`).

## Performance Testing

For load testing, consider tools like:
- **JMeter**: HTTP load testing
- **Gatling**: Scala-based load testing
- **k6**: Modern load testing tool

Example k6 script:

```javascript
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 10,
  duration: '30s',
};

export default function () {
  let res = http.get('http://localhost:8080/api/v1/meals');
  check(res, { 'status is 200': (r) => r.status === 200 });
}
```

## Further Reading

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Selenium WebDriver Documentation](https://www.selenium.dev/documentation/webdriver/)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)

---

**Happy Testing! 🧪**
