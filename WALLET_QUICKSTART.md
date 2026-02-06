# Wallet System - Quick Start Guide

## Getting Started with Wallet Operations

### Step 1: Authenticate

First, get a JWT token by logging in:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "Test@1234"
  }'
```

**Response**:
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

Store the `accessToken` for all wallet operations.

---

### Step 2: Check Your Balance

Get current wallet balance:

```bash
curl -X GET http://localhost:8080/api/wallets/balance/GOLD_COINS \
  -H "Authorization: Bearer {accessToken}"
```

**Response**:
```json
{
  "status": 200,
  "message": "Balance retrieved successfully",
  "data": {
    "assetCode": "GOLD_COINS",
    "assetName": "Gold Coins",
    "balance": 500.00
  }
}
```

---

### Step 3: Top-Up Your Wallet

Purchase credits from the system treasury:

```bash
curl -X POST http://localhost:8080/api/wallets/top-up \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "assetCode": "GOLD_COINS",
    "amount": 100.00,
    "idempotencyKey": "550e8400-e29b-41d4-a716-446655440001",
    "description": "Purchase 100 gold coins"
  }'
```

**Response** (201 Created):
```json
{
  "status": 201,
  "message": "Top-up successful",
  "data": {
    "transactionId": 1,
    "idempotencyKey": "550e8400-e29b-41d4-a716-446655440001",
    "amount": 100.00,
    "transactionType": "TOP_UP",
    "debitWalletId": 7,
    "creditWalletId": 2,
    "newDebitBalance": 999900.00,
    "newCreditBalance": 600.00,
    "createdAt": "2026-02-05T12:45:00"
  }
}
```

---

### Step 4: Receive a Bonus

Get system-issued credits:

```bash
curl -X POST http://localhost:8080/api/wallets/bonus \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "assetCode": "LOYALTY_POINTS",
    "amount": 50.00,
    "idempotencyKey": "660e8400-e29b-41d4-a716-446655440002",
    "description": "Referral bonus"
  }'
```

---

### Step 5: Spend Credits

Use your credits:

```bash
curl -X POST http://localhost:8080/api/wallets/spend \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "assetCode": "GOLD_COINS",
    "amount": 50.00,
    "idempotencyKey": "770e8400-e29b-41d4-a716-446655440003",
    "description": "Purchase in-game item"
  }'
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Spend successful",
  "data": {
    "transactionId": 3,
    "amount": 50.00,
    "transactionType": "SPEND",
    "newCreditBalance": 550.00,
    "createdAt": "2026-02-05T12:46:00"
  }
}
```

---

## Available Assets

| Asset Code | Display Name | Type | Initial Balance |
|-----------|--------------|------|-----------------|
| GOLD_COINS | Gold Coins | Premium Currency | 500.00 |
| LOYALTY_POINTS | Loyalty Points | Rewards | 1000.00 |
| CREDIT_TOKENS | Credit Tokens | Service Credits | 100.00 |

---

## Idempotency Key

**Important**: Always provide a unique `idempotencyKey` for each request.

This ensures that if your request times out or is retried, the transaction won't be duplicated:

```bash
# First attempt
idempotencyKey: "550e8400-e29b-41d4-a716-446655440000"

# Retry with same key → Returns cached result (no double-charge)
idempotencyKey: "550e8400-e29b-41d4-a716-446655440000"

# Different key
idempotencyKey: "550e8400-e29b-41d4-a716-446655440001"
```

**Generate UUIDs**:
- Online: https://www.uuidgenerator.net/
- Or use your client's UUID library

---

## Error Responses

### Insufficient Balance
```json
{
  "status": 400,
  "message": "Insufficient balance. Available: 30.00, Required: 50.00"
}
```

### Asset Not Found
```json
{
  "status": 404,
  "message": "Asset not found: UNKNOWN_ASSET"
}
```

### Missing JWT Token
```json
{
  "status": 401,
  "message": "Unauthorized"
}
```

### Invalid Input
```json
{
  "status": 400,
  "message": "Amount must be greater than 0"
}
```

---

## Testing with Postman

1. **Import Collection**:
   - Open Postman
   - Import `Money_Manager_Postman_Collection.json`

2. **Set Environment Variables**:
   - BASE_URL: `http://localhost:8080`
   - TOKEN: (from login response)

3. **Test Endpoints**:
   - Login → Get Token
   - Check Balance
   - Top-Up
   - Receive Bonus
   - Spend Credits

---

## Rate Limits & Constraints

- **Max Amount**: 999,999,999.99
- **Min Amount**: 0.01
- **Decimals**: 2 (e.g., 123.45)
- **Idempotency Key Length**: 100 characters max
- **Request Timeout**: 30 seconds

---

## Troubleshooting

### "User not authenticated"
- Ensure JWT token is included in Authorization header
- Format: `Authorization: Bearer {token}`
- Token expires after 24 hours (86400000 ms)

### "Wallet not found"
- User might not have been initialized with wallet for that asset
- Try registering a new user and logging in

### "Duplicate key"
- Idempotency key was already used
- Use a new UUID for new transactions

### "System treasury depleted"
- Reset the application to reload seed data
- Or implement admin endpoint to refill treasury

---

## Best Practices

1. **Always use idempotency keys**: Prevents accidental duplicates
2. **Validate balance before spending**: Check `/balance` endpoint first
3. **Log transaction IDs**: For reconciliation and debugging
4. **Retry with exponential backoff**: Use same idempotency key
5. **Handle all status codes**: 201, 200, 400, 401, 404, 500

---

## API Contract

All responses follow this format:

```json
{
  "status": <HTTP_STATUS_CODE>,
  "message": "<HUMAN_READABLE_MESSAGE>",
  "data": <RESPONSE_PAYLOAD>,
  "timestamp": "<ISO_8601_TIMESTAMP>"
}
```

---

## Example Workflow

```bash
#!/bin/bash

# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john.doe@example.com","password":"Test@1234"}' \
  | jq -r '.data.accessToken')

echo "Token: $TOKEN"

# 2. Check initial balance
curl -X GET http://localhost:8080/api/wallets/balance/GOLD_COINS \
  -H "Authorization: Bearer $TOKEN" | jq

# 3. Top-up
curl -X POST http://localhost:8080/api/wallets/top-up \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "assetCode":"GOLD_COINS",
    "amount":100,
    "idempotencyKey":"'$(uuidgen)'",
    "description":"Purchased via CLI"
  }' | jq

# 4. Check balance after top-up
curl -X GET http://localhost:8080/api/wallets/balance/GOLD_COINS \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## Need Help?

- Check `WALLET_README.md` for detailed architecture documentation
- Review `API_DOCUMENTATION.md` for complete API reference
- Check application logs in `logs/app.log`
- Ensure H2 database is accessible at `http://localhost:8080/h2-console`

---

**Version**: 1.0  
**Last Updated**: February 5, 2026
