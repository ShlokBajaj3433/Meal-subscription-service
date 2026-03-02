# API Documentation

Complete REST API reference for the Meal Subscription Service.

## Table of Contents

- [Overview](#overview)
- [Authentication](#authentication)
- [Error Responses](#error-responses)
- [Endpoints](#endpoints)
  - [Authentication API](#authentication-api)
  - [Users API](#users-api)
  - [Meals API](#meals-api)
  - [Subscriptions API](#subscriptions-api)
  - [Payments API](#payments-api)
  - [Admin API](#admin-api)
- [Frontend Routes](#frontend-routes)

## Overview

**Base URL**: `http://localhost:8080/api/v1`

**Content Type**: All requests and responses use `application/json` unless otherwise specified.

**Date Format**: ISO 8601 (UTC): `2026-03-02T18:24:26.262Z`

## Authentication

Most endpoints require authentication via JWT (JSON Web Token).

### Obtaining a Token

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER"
}
```

### Using the Token

Include the token in the `Authorization` header:

```http
GET /api/v1/users/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Token Expiration

Tokens expire after **24 hours**. The client should handle 401 Unauthorized responses and prompt for re-authentication.

## Error Responses

The API uses RFC 9457 Problem Details for HTTP APIs format for error responses.

### Standard Error Format

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Meal with id 999 not found",
  "instance": "/api/v1/meals/999"
}
```

### Common HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET/PUT request |
| 201 | Created | Successful POST creating a resource |
| 204 | No Content | Successful DELETE request |
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Authenticated but lacking permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource (e.g., email already registered) |
| 422 | Unprocessable Entity | Validation errors |
| 500 | Internal Server Error | Server-side error |

### Validation Error Format

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 422,
  "detail": "Request validation failed",
  "errors": {
    "email": "must be a well-formed email address",
    "password": "size must be between 8 and 100"
  }
}
```

---

## Endpoints

## Authentication API

### Register User

Create a new user account.

```http
POST /api/v1/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123"
}
```

**Validation Rules:**
- `name`: Required, 2-100 characters
- `email`: Required, valid email format, unique
- `password`: Required, 8-100 characters

**Success Response (201 Created):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "role": "USER",
  "isActive": true,
  "createdAt": "2026-03-02T10:30:00Z"
}
```

**Error Response (409 Conflict):**
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "User with email john.doe@example.com already exists"
}
```

---

### Login

Authenticate and receive a JWT token.

```http
POST /api/v1/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePass123"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsInJvbGVzIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzA5Mzc5MDAwLCJleHAiOjE3MDk0NjU0MDB9.signature",
  "email": "john.doe@example.com",
  "name": "John Doe",
  "role": "USER"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Invalid email or password"
}
```

---

## Users API

### Get Current User

Get the authenticated user's profile.

```http
GET /api/v1/users/me
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "role": "USER",
  "isActive": true,
  "createdAt": "2026-03-02T10:30:00Z",
  "updatedAt": "2026-03-02T10:30:00Z"
}
```

---

## Meals API

### List Meals

Get a list of available meals with optional filtering.

```http
GET /api/v1/meals?dietary={type}&page=0&size=20&sort=name,asc
```

**Query Parameters:**
- `dietary` (optional): Filter by dietary type (`STANDARD`, `VEGETARIAN`, `VEGAN`, `GLUTEN_FREE`, `KETO`)
- `page` (optional, default: 0): Page number (zero-indexed)
- `size` (optional, default: 20): Page size
- `sort` (optional, default: `name,asc`): Sort criteria

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Grilled Chicken Salad",
      "description": "Fresh mixed greens with grilled chicken breast",
      "dietaryType": "STANDARD",
      "calories": 350,
      "priceCents": 1299,
      "imageUrl": "https://example.com/images/chicken-salad.jpg",
      "isAvailable": true
    },
    {
      "id": 2,
      "name": "Quinoa Buddha Bowl",
      "description": "Nutritious quinoa with roasted vegetables",
      "dietaryType": "VEGAN",
      "calories": 420,
      "priceCents": 1199,
      "imageUrl": "https://example.com/images/buddha-bowl.jpg",
      "isAvailable": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 15,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 15
}
```

---

### Get Meal by ID

Get details of a specific meal.

```http
GET /api/v1/meals/{id}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "name": "Grilled Chicken Salad",
  "description": "Fresh mixed greens with grilled chicken breast, cherry tomatoes, and balsamic vinaigrette",
  "dietaryType": "STANDARD",
  "calories": 350,
  "priceCents": 1299,
  "imageUrl": "https://example.com/images/chicken-salad.jpg",
  "isAvailable": true
}
```

**Error Response (404 Not Found):**
```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Meal with id 999 not found"
}
```

---

### Create Meal (Admin Only)

Create a new meal.

```http
POST /api/v1/meals
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Mediterranean Pasta",
  "description": "Whole wheat pasta with sun-dried tomatoes, olives, and feta cheese",
  "dietaryType": "VEGETARIAN",
  "calories": 480,
  "priceCents": 1399,
  "imageUrl": "https://example.com/images/mediterranean-pasta.jpg",
  "isAvailable": true
}
```

**Validation Rules:**
- `name`: Required, 3-100 characters, unique
- `description`: Required, 10-500 characters
- `dietaryType`: Required, must be valid enum value
- `calories`: Required, > 0, < 5000
- `priceCents`: Required, > 0
- `imageUrl`: Optional, valid URL format
- `isAvailable`: Required

**Success Response (201 Created):**
```json
{
  "id": 16,
  "name": "Mediterranean Pasta",
  "description": "Whole wheat pasta with sun-dried tomatoes, olives, and feta cheese",
  "dietaryType": "VEGETARIAN",
  "calories": 480,
  "priceCents": 1399,
  "imageUrl": "https://example.com/images/mediterranean-pasta.jpg",
  "isAvailable": true
}
```

---

### Update Meal (Admin Only)

Update an existing meal.

```http
PUT /api/v1/meals/{id}
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**Request Body:** Same as Create Meal

**Success Response (200 OK):** Returns updated meal object

---

### Delete Meal (Admin Only)

Delete a meal.

```http
DELETE /api/v1/meals/{id}
Authorization: Bearer {admin-token}
```

**Success Response (204 No Content):** Empty body

---

## Subscriptions API

### Create Subscription

Create a new subscription.

```http
POST /api/v1/subscriptions
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "planType": "WEEKLY",
  "mealSelections": [
    {
      "mealId": 1,
      "deliveryDate": "2026-03-10",
      "quantity": 2
    },
    {
      "mealId": 3,
      "deliveryDate": "2026-03-12",
      "quantity": 1
    }
  ]
}
```

**Validation Rules:**
- `planType`: Required, `WEEKLY` or `MONTHLY`
- `mealSelections`: Required, at least 1 item
- `mealId`: Required, must exist
- `deliveryDate`: Required, future date
- `quantity`: Required, 1-10

**Success Response (201 Created):**
```json
{
  "id": 42,
  "userId": 1,
  "planType": "WEEKLY",
  "status": "ACTIVE",
  "startDate": "2026-03-10",
  "endDate": "2026-03-17",
  "stripeSubscriptionId": "sub_1234567890",
  "meals": [
    {
      "mealId": 1,
      "mealName": "Grilled Chicken Salad",
      "deliveryDate": "2026-03-10",
      "quantity": 2
    },
    {
      "mealId": 3,
      "mealName": "Quinoa Buddha Bowl",
      "deliveryDate": "2026-03-12",
      "quantity": 1
    }
  ],
  "createdAt": "2026-03-02T15:30:00Z"
}
```

---

### List User Subscriptions

Get all subscriptions for the authenticated user.

```http
GET /api/v1/subscriptions?page=0&size=20&sort=createdAt,desc
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": 42,
      "planType": "WEEKLY",
      "status": "ACTIVE",
      "startDate": "2026-03-10",
      "endDate": "2026-03-17",
      "createdAt": "2026-03-02T15:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### Get Subscription Details

Get details of a specific subscription.

```http
GET /api/v1/subscriptions/{id}
Authorization: Bearer {token}
```

**Success Response (200 OK):** Returns full subscription object with meals

---

### Pause Subscription

Pause an active subscription.

```http
POST /api/v1/subscriptions/{id}/pause
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "id": 42,
  "status": "PAUSED",
  "message": "Subscription paused successfully"
}
```

**Error Response (400 Bad Request):**
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Cannot pause subscription in CANCELLED state"
}
```

---

### Resume Subscription

Resume a paused subscription.

```http
POST /api/v1/subscriptions/{id}/resume
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "id": 42,
  "status": "ACTIVE",
  "message": "Subscription resumed successfully"
}
```

---

### Cancel Subscription

Cancel a subscription.

```http
POST /api/v1/subscriptions/{id}/cancel
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "id": 42,
  "status": "CANCELLED",
  "message": "Subscription cancelled successfully"
}
```

---

## Payments API

### Stripe Webhook Handler

Receive and process Stripe webhook events.

```http
POST /api/v1/payments/webhook
Content-Type: application/json
Stripe-Signature: t=1234567890,v1=signature_here
```

**Note:** This endpoint is public but validates the Stripe signature. Only Stripe should call this endpoint.

**Handled Events:**
- `payment_intent.succeeded` - Payment successful
- `payment_intent.payment_failed` - Payment failed
- `invoice.payment_succeeded` - Recurring payment successful
- `invoice.payment_failed` - Recurring payment failed

**Success Response (200 OK):**
```json
{
  "received": true
}
```

---

### List User Payments

Get payment history for the authenticated user.

```http
GET /api/v1/payments?page=0&size=20&sort=paidAt,desc
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": 100,
      "subscriptionId": 42,
      "stripePaymentIntentId": "pi_1234567890",
      "amountCents": 3897,
      "currency": "usd",
      "status": "SUCCEEDED",
      "paidAt": "2026-03-02T16:00:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

---

### List All Payments (Admin Only)

Get all payments across all users.

```http
GET /api/v1/payments/all?page=0&size=50&sort=paidAt,desc
Authorization: Bearer {admin-token}
```

**Success Response (200 OK):** Paginated payment list with user information

---

## Admin API

### List All Users

Get a paginated list of all users.

```http
GET /api/v1/admin/users?page=0&size=50&sort=createdAt,desc
Authorization: Bearer {admin-token}
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "role": "USER",
      "isActive": true,
      "createdAt": "2026-03-01T10:00:00Z",
      "updatedAt": "2026-03-01T10:00:00Z"
    }
  ],
  "totalElements": 125,
  "totalPages": 3
}
```

---

### Get User Details (Admin Only)

Get details of a specific user.

```http
GET /api/v1/admin/users/{id}
Authorization: Bearer {admin-token}
```

**Success Response (200 OK):** Returns user object with subscription count

---

### Deactivate User (Admin Only)

Deactivate a user account.

```http
DELETE /api/v1/admin/users/{id}
Authorization: Bearer {admin-token}
```

**Success Response (204 No Content):** Empty body

**Note:** This soft-deletes the user (sets `isActive = false`) rather than permanently deleting.

---

### List All Subscriptions (Admin Only)

Get all subscriptions across all users.

```http
GET /api/v1/admin/subscriptions?page=0&size=50&sort=createdAt,desc
Authorization: Bearer {admin-token}
```

**Success Response (200 OK):** Paginated subscription list with user information

---

### Get Subscription Details (Admin Only)

Get details of any subscription.

```http
GET /api/v1/admin/subscriptions/{id}
Authorization: Bearer {admin-token}
```

**Success Response (200 OK):** Returns full subscription object

---

## Frontend Routes

These routes serve Thymeleaf HTML pages (not REST API).

### Public Routes

| Route | Method | Description |
|-------|--------|-------------|
| `/` | GET | Redirects to `/login` |
| `/login` | GET | Login page |
| `/register` | GET | Registration page |
| `/web/login` | POST | Process login (sets JWT cookie) |
| `/web/register` | POST | Process registration |

### Authenticated Routes

| Route | Method | Description |
|-------|--------|-------------|
| `/dashboard` | GET | User dashboard |
| `/meals` | GET | Browse meals (supports `?dietary=` filter) |

### Admin Routes

| Route | Method | Description |
|-------|--------|-------------|
| `/admin/meals` | GET | Manage meals (CRUD interface) |

**Note:** Frontend routes set a JWT in a non-HttpOnly cookie named `jwt_token` which JavaScript can read to make API calls.

---

## Rate Limiting

Currently, there is no built-in rate limiting. For production, consider implementing rate limiting at:
- API Gateway level
- Application level (Spring Security with Bucket4j)
- Web server level (Nginx limit_req)

## API Versioning

The API is versioned via URL path (`/api/v1/`). Future versions will use `/api/v2/`, etc.

## CORS

CORS is configurable via the `app.cors.allowed-origins` property. Default allows:
- `http://localhost:3000` (for React/Vue frontends)
- `http://localhost:8080` (same-origin)

## Support

For API questions or issues, please open a GitHub issue or contact the development team.
