# Money Manager Backend - API Documentation

## Overview
This is a comprehensive REST API for managing personal finances with user authentication, transaction tracking, and budget management.

## Base URL
```
http://localhost:8080/api
```

## Authentication
All protected endpoints require JWT token in the Authorization header:
```
Authorization: Bearer <access_token>
```

---

## Authentication Endpoints

### 1. Register User
**POST** `/auth/register`

Register a new user account.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass@123",
  "confirmPassword": "SecurePass@123"
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 2. Login
**POST** `/auth/login`

Authenticate user and receive JWT tokens.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass@123"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 3. Refresh Token
**POST** `/auth/refresh`

Get a new access token using refresh token.

**Headers:**
```
Authorization: Bearer <refresh_token>
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## User Endpoints

### 1. Get User Profile
**GET** `/users/profile`

Retrieve authenticated user's profile information.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "User profile retrieved successfully",
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "active": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 2. Update User Profile
**PUT** `/users/profile`

Update authenticated user's profile.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "firstName": "Johnny",
  "lastName": "Smith",
  "active": true
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "User profile updated successfully",
  "data": {
    "id": 1,
    "firstName": "Johnny",
    "lastName": "Smith",
    "email": "john@example.com",
    "active": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:35:00"
  },
  "timestamp": "2024-01-15T10:35:00"
}
```

---

### 3. Delete User
**DELETE** `/users/profile`

Delete authenticated user's account.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "User deleted successfully",
  "data": null,
  "timestamp": "2024-01-15T10:40:00"
}
```

---

## Transaction Endpoints

### 1. Create Transaction
**POST** `/transactions`

Create a new financial transaction.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "type": "EXPENSE",
  "category": "Food & Dining",
  "amount": 45.50,
  "description": "Lunch at restaurant",
  "transactionDate": "2024-01-15T12:30:00"
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Transaction created successfully",
  "data": {
    "id": 1,
    "type": "EXPENSE",
    "category": "Food & Dining",
    "amount": 45.50,
    "description": "Lunch at restaurant",
    "transactionDate": "2024-01-15T12:30:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 2. Get All Transactions
**GET** `/transactions`

Retrieve paginated list of all user transactions.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Records per page
- `sortBy` (default: transactionDate) - Field to sort by
- `direction` (default: DESC) - ASC or DESC

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transactions retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "type": "EXPENSE",
        "category": "Food & Dining",
        "amount": 45.50,
        "description": "Lunch",
        "transactionDate": "2024-01-15T12:30:00",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0,
    "empty": false
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 3. Get Transactions by Type
**GET** `/transactions/type/{type}`

Get transactions filtered by type (INCOME or EXPENSE).

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `type` - INCOME or EXPENSE

**Response (200 OK):** Same as Get All Transactions

---

### 4. Get Transaction by ID
**GET** `/transactions/{id}`

Retrieve specific transaction details.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `id` - Transaction ID

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transaction retrieved successfully",
  "data": {
    "id": 1,
    "type": "EXPENSE",
    "category": "Food & Dining",
    "amount": 45.50,
    "description": "Lunch",
    "transactionDate": "2024-01-15T12:30:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 5. Update Transaction
**PUT** `/transactions/{id}`

Update an existing transaction.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `id` - Transaction ID

**Request Body:**
```json
{
  "type": "EXPENSE",
  "category": "Entertainment",
  "amount": 35.00,
  "description": "Movie tickets",
  "transactionDate": "2024-01-15T18:00:00"
}
```

**Response (200 OK):** Same as Get Transaction by ID

---

### 6. Delete Transaction
**DELETE** `/transactions/{id}`

Delete a transaction.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `id` - Transaction ID

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transaction deleted successfully",
  "data": null,
  "timestamp": "2024-01-15T10:40:00"
}
```

---

## Budget Endpoints

### 1. Create Budget
**POST** `/budgets`

Create a new budget for a category.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "category": "Food & Dining",
  "limit": 500.00,
  "monthYear": "2024-01"
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Budget created successfully",
  "data": {
    "id": 1,
    "category": "Food & Dining",
    "limit": 500.00,
    "monthYear": "2024-01",
    "active": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 2. Get All Budgets
**GET** `/budgets`

Retrieve paginated list of user budgets.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Records per page

**Response (200 OK):** Similar to transactions list response

---

### 3. Get Active Budgets
**GET** `/budgets/active`

Retrieve only active budgets.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):** Similar to Get All Budgets

---

### 4. Get Budget by ID
**GET** `/budgets/{id}`

Retrieve specific budget details.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `id` - Budget ID

**Response (200 OK):** Same as Create Budget response

---

### 5. Update Budget
**PUT** `/budgets/{id}`

Update an existing budget.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `id` - Budget ID

**Request Body:**
```json
{
  "category": "Food & Dining",
  "limit": 600.00,
  "monthYear": "2024-01",
  "active": true
}
```

**Response (200 OK):** Same as Get Budget by ID

---

### 6. Delete Budget
**DELETE** `/budgets/{id}`

Delete a budget.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**
- `id` - Budget ID

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Budget deleted successfully",
  "data": null,
  "timestamp": "2024-01-15T10:40:00"
}
```

---

## Health Endpoint

### Health Check
**GET** `/health`

Check application health status.

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Application is running successfully",
  "data": "HEALTHY",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "status": 400,
  "message": "Validation failed",
  "data": {
    "email": "Email should be valid",
    "password": "Password must be between 8 and 100 characters"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "Transaction not found",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 409 Conflict
```json
{
  "status": 409,
  "message": "Email already registered",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "An error occurred. Please try again later.",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Missing/invalid token |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error |

---

## Rate Limiting

Currently no rate limiting is implemented. Can be added using:
- Spring Cloud Config
- Redis for distributed rate limiting
- Custom interceptors

---

## Pagination

All list endpoints support pagination with the following response structure:

```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0,
  "empty": false
}
```

---

## Transaction Types

- **INCOME** - Money coming in
- **EXPENSE** - Money going out

---

## Sample Categories

- Food & Dining
- Transportation
- Entertainment
- Shopping
- Bills & Utilities
- Healthcare
- Education
- Savings
- Other

---

**Last Updated**: January 2024
