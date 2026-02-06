# Wallet System - Architecture & Design Document

## Overview

The Money Manager application now includes a **ledger-driven, closed-loop wallet system** that manages virtual credits (Gold Coins, Loyalty Points, Credit Tokens) using double-entry bookkeeping principles. This system is designed for high-traffic environments with strong consistency guarantees.

---

## Architecture

### Core Components

#### 1. **Asset Management** (`wallet.asset` package)
- **Entity**: `Asset`
- **Purpose**: Define types of virtual currencies available in the system
- **Fields**:
  - `code`: Unique identifier (e.g., GOLD_COINS, LOYALTY_POINTS)
  - `name`: Human-readable name
  - `type`: Asset category (enum)
  - `active`: Soft-delete flag
  - `created_at`, `updated_at`: Audit timestamps

**Supported Assets**:
- GOLD_COINS - Premium currency
- LOYALTY_POINTS - Reward points
- CREDIT_TOKENS - Service credits
- PREMIUM_CURRENCY - Premium tier currency

#### 2. **Wallet System** (`wallet` package)
- **Entity**: `Wallet`
- **Purpose**: Store account balances per asset
- **Key Features**:
  - Supports both **User Wallets** (linked to User entity) and **System Wallets** (treasury, bonus pool)
  - Balance is always non-negative (enforced at DB level)
  - Optimistic locking via `@Version` for concurrent updates
  - Unique constraint ensures one wallet per (user, asset) pair

**Wallet Types**:
```
User Wallet:
  - user_id: NOT NULL
  - system_wallet_id: NULL
  - is_system_wallet: false

System Wallet (TREASURY):
  - user_id: NULL
  - system_wallet_id: 'TREASURY'
  - is_system_wallet: true

System Wallet (BONUS_POOL):
  - user_id: NULL
  - system_wallet_id: 'BONUS_POOL'
  - is_system_wallet: true
```

#### 3. **Ledger System** (`wallet.ledger` package)
- **Entity**: `LedgerEntry`
- **Purpose**: Immutable audit trail using double-entry bookkeeping
- **Key Features**:
  - Every transaction creates exactly ONE ledger entry
  - Records both debit and credit sides
  - `idempotencyKey`: Ensures no duplicate entries for retried requests
  - Indexed on debit_wallet, credit_wallet, idempotency_key, and created_at

**Transaction Types**:
- `TOP_UP`: User wallet ← Treasury (purchase)
- `BONUS`: User wallet ← Bonus Pool (promotion)
- `SPEND`: User wallet → Treasury (usage)
- `TRANSFER`: User wallet → User wallet (P2P)
- `REFUND`: Treasury → User wallet (reversal)

---

## Concurrency & Atomicity Strategy

### Problem: Race Conditions in Concurrent Environments

In high-traffic scenarios, multiple requests can modify wallet balances simultaneously, risking:
- Lost updates (concurrent writes overwrite each other)
- Negative balances (concurrent spends exceed balance)
- Double-charging via duplicate requests

### Solution: SERIALIZABLE Isolation with SELECT FOR UPDATE

#### Implementation Details

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public WalletTransactionResult topUp(User user, String assetCode, BigDecimal amount, String idempotencyKey) {
    // All wallet operations use pessimistic locking
    Wallet userWallet = walletRepository.findUserWalletForUpdate(user, asset);
    Wallet systemWallet = walletRepository.findSystemWalletForUpdate("TREASURY", asset);
    
    // Balance updates within same transaction
    systemWallet.setBalance(systemWallet.getBalance().subtract(amount));
    userWallet.setBalance(userWallet.getBalance().add(amount));
    
    // Ledger entry created atomically
    LedgerEntry ledger = new LedgerEntry(...);
    ledgerRepository.save(ledger);
}
```

#### Concurrency Features

| Feature | Method | Benefit |
|---------|--------|---------|
| **Pessimistic Locking** | `SELECT ... FOR UPDATE` | Locks rows during read, prevents conflicts |
| **SERIALIZABLE Isolation** | DB transaction isolation level | Prevents dirty reads, phantom reads |
| **Atomic Operations** | Single `@Transactional` block | All-or-nothing wallet updates |
| **Version Control** | `@Version` field on Wallet | Optimistic lock fallback |
| **Unique Idempotency Key** | Database unique constraint | Prevents duplicate ledger entries |

#### Read Operations (Safe Without Locking)
```java
@Transactional(readOnly = true)
public BigDecimal getBalance(User user, String assetCode) {
    // Read-only queries don't require locks
    Wallet wallet = walletRepository.findByUserAndAsset(user, asset);
    return wallet.getBalance();
}
```

---

## Idempotency

### Problem: Network Failures & Retries

Clients may retry failed requests, risking duplicate charges or credits.

### Solution: Idempotency Key Pattern

Every wallet operation requires an **idempotencyKey** (UUID) provided by the client:

```json
{
  "assetCode": "GOLD_COINS",
  "amount": 100.00,
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
  "description": "Purchase gold coins"
}
```

#### Flow
1. **First Request**: Creates ledger entry with idempotencyKey
2. **Retry Request**: Detects existing idempotencyKey, returns cached result
3. **Result**: No duplicate wallet modifications

#### Database Guarantee
```sql
-- Unique constraint on ledger table
CREATE UNIQUE INDEX idx_ledger_idempotency_key ON ledger_entries(idempotency_key);
```

#### Implementation
```java
public WalletTransactionResult topUp(..., String idempotencyKey) {
    // Check if transaction already exists
    Optional<LedgerEntry> existing = ledgerRepository.findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
        return mapToResult(existing.get());  // Return previous result
    }
    
    // Process new transaction
    // ...
}
```

---

## Wallet Operations

### API Endpoints

All endpoints are under `/api/wallets` and require JWT authentication.

#### 1. Top-Up (Treasury → User)
```http
POST /api/wallets/top-up
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "assetCode": "GOLD_COINS",
  "amount": 500.00,
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
  "description": "Purchase gold pack"
}
```

**Response** (201 Created):
```json
{
  "status": 201,
  "message": "Top-up successful",
  "data": {
    "transactionId": 1,
    "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 500.00,
    "transactionType": "TOP_UP",
    "debitWalletId": 1,
    "creditWalletId": 2,
    "newDebitBalance": 999500.00,
    "newCreditBalance": 500.00,
    "createdAt": "2026-02-05T12:00:00"
  }
}
```

#### 2. Bonus (Bonus Pool → User)
```http
POST /api/wallets/bonus
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "assetCode": "LOYALTY_POINTS",
  "amount": 100.00,
  "idempotencyKey": "660e8400-e29b-41d4-a716-446655440001",
  "description": "Referral bonus"
}
```

#### 3. Spend (User → Treasury)
```http
POST /api/wallets/spend
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "assetCode": "GOLD_COINS",
  "amount": 50.00,
  "idempotencyKey": "770e8400-e29b-41d4-a716-446655440002",
  "description": "Purchase in-game item"
}
```

**Error Response** (400 Bad Request - Insufficient Balance):
```json
{
  "status": 400,
  "message": "Insufficient balance. Available: 30.00, Required: 50.00"
}
```

#### 4. Get Balance
```http
GET /api/wallets/balance/{assetCode}
Authorization: Bearer {jwt_token}
```

**Response** (200 OK):
```json
{
  "status": 200,
  "message": "Balance retrieved successfully",
  "data": {
    "assetCode": "GOLD_COINS",
    "assetName": "Gold Coins",
    "balance": 450.00
  }
}
```

---

## Data Seeding

### Initial Setup

**Assets Created**:
- GOLD_COINS (500.00 per user)
- LOYALTY_POINTS (1000.00 per user)
- CREDIT_TOKENS (100.00 per user)

**System Wallets**:
- TREASURY: 1,000,000.00 (each asset)
- BONUS_POOL: 500,000.00 (each asset)

**Sample User Wallets**:
- john.doe@example.com: Initialized with default balances

### Seed Process

The `WalletDataLoader` component runs on application startup:

```java
@Component
public class WalletDataLoader implements CommandLineRunner {
    public void run(String... args) {
        initializeAssets();        // Create all asset types
        initializeSystemWallets();  // Create TREASURY and BONUS_POOL
        initializeUserWallets();    // Create wallets for all existing users
    }
}
```

**Idempotency**: Loader checks for existing records before creating duplicates.

---

## Transaction Safety Guarantees

### ACID Compliance

| Property | Implementation | Guarantee |
|----------|----------------|-----------|
| **Atomicity** | `@Transactional`, single DB transaction | All-or-nothing: ledger + balance or nothing |
| **Consistency** | Constraints: non-negative balance, unique wallets | Balance never negative, no duplicates |
| **Isolation** | `SERIALIZABLE`, pessimistic locking | No dirty reads, phantom reads, or lost updates |
| **Durability** | Database persistence | Changes survive application crashes |

### Failure Scenarios

#### Scenario 1: Network Failure After Balance Update
```
1. Client sends spend request (idempotencyKey: X)
2. Server processes: deducts from wallet, creates ledger entry
3. Network timeout before response reaches client
4. Client retries with same idempotencyKey
5. Server detects existing ledger entry, returns cached result
✓ No duplicate deduction
```

#### Scenario 2: Concurrent Spend Requests
```
User has 100 coins, two concurrent $60 spends:

Thread 1: SELECT wallet FOR UPDATE (balance=100, locks row)
Thread 2: Waits for lock...
Thread 1: UPDATE wallet SET balance=40
Thread 2: Acquires lock (balance=40)
Thread 2: Tries to deduct 60, insufficient balance → ERROR
✓ No negative balance
```

#### Scenario 3: System Wallet Depletion
```
Spend request when TREASURY balance is low:
1. User wallet locked: 50 coins
2. System wallet locked: 5 coins
3. Transaction requires 60 coins spend
4. Transaction fails: "Insufficient system treasury"
5. Both wallets roll back
✓ System state consistent
```

---

## Performance Considerations

### Indexing Strategy

```sql
CREATE INDEX idx_ledger_debit_wallet ON ledger_entries(debit_wallet_id);
CREATE INDEX idx_ledger_credit_wallet ON ledger_entries(credit_wallet_id);
CREATE UNIQUE INDEX idx_ledger_idempotency_key ON ledger_entries(idempotency_key);
CREATE INDEX idx_ledger_created_at ON ledger_entries(created_at);
```

### Lock Contention Mitigation

1. **Short Transaction Windows**: Lock held only during balance update
2. **Row-Level Locking**: Only affected wallets are locked
3. **Read-Only Queries**: `getBalance()` doesn't acquire locks
4. **Asynchronous Operations**: Future: Move audit logging to event queue

### Expected Performance

- **Balance Query**: ~5-10ms (no lock)
- **Spend Operation**: ~50-100ms (with lock contention)
- **System Throughput**: 100-500 TPS (depends on DB)

---

## Backward Compatibility

### Existing Features Unchanged

✓ User authentication (JWT)
✓ Transaction management (payments)
✓ Budget management
✓ All existing endpoints
✓ User profiles
✓ Health checks

### New Features Isolated

- New packages: `wallet.*` package hierarchy
- New tables: `assets`, `wallets`, `ledger_entries`
- No modifications to existing entities or repositories
- No changes to existing DTOs or controllers

---

## Future Enhancements

1. **Event-Driven Auditing**: Use Kafka/RabbitMQ for ledger notifications
2. **Transfer Between Users**: `POST /api/wallets/transfer`
3. **Wallet History API**: Paginated transaction history
4. **Settlement Batching**: End-of-day reconciliation jobs
5. **Rate Limiting**: API rate limiting per user
6. **Blockchain Integration**: Optional: Immutable ledger on blockchain
7. **Multi-Currency Support**: Conversion rates between assets

---

## Testing Recommendations

### Unit Tests
```java
@Test
public void testConcurrentSpends() {
    // Two threads spend simultaneously
    // Verify only one succeeds
}

@Test
public void testIdempotency() {
    // Send same request twice
    // Verify no duplicate ledger entry
}
```

### Integration Tests
```java
@Test
public void testInsufficientBalance() {
    // Spend more than available
    // Verify error and balance unchanged
}
```

### Load Tests
```
ApacheBench: ab -n 10000 -c 100 http://localhost:8080/api/wallets/balance/GOLD_COINS
Verify: No negative balances, all requests succeed
```

---

## Monitoring & Alerts

### Key Metrics to Track

1. **System Wallet Balance**: TREASURY and BONUS_POOL should not deplete
2. **Ledger Entry Count**: Growing ledger size
3. **Transaction Latency**: p50, p95, p99 latency
4. **Error Rate**: Failed spends (insufficient balance)
5. **Lock Contention**: DB lock wait time

### Suggested Alerts

- Treasury balance < 10% of initial: Critical
- Ledger entry creation latency > 500ms: Warning
- Failed spend rate > 1%: Alert

---

## Security Considerations

1. **JWT Token**: All wallet operations require valid JWT
2. **User Isolation**: Users can only access their own wallets
3. **Audit Trail**: Every transaction recorded in ledger
4. **No Negative Balance**: Database constraint + application validation
5. **Idempotency**: Prevents replay attacks for duplicate requests
6. **Transaction Isolation**: SERIALIZABLE prevents concurrent exploits

---

## Database Schema

```sql
CREATE TABLE assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    asset_id BIGINT NOT NULL,
    balance DECIMAL(19,2) NOT NULL CHECK (balance >= 0),
    system_wallet_id VARCHAR(50),
    is_system_wallet BOOLEAN DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_asset (user_id, asset_id),
    UNIQUE KEY unique_system_asset (system_wallet_id, asset_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES assets(id)
);

CREATE TABLE ledger_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    debit_wallet_id BIGINT NOT NULL,
    credit_wallet_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (debit_wallet_id) REFERENCES wallets(id),
    FOREIGN KEY (credit_wallet_id) REFERENCES wallets(id),
    INDEX idx_ledger_debit_wallet (debit_wallet_id),
    INDEX idx_ledger_credit_wallet (credit_wallet_id),
    INDEX idx_ledger_created_at (created_at)
);
```

---

## Troubleshooting

### Issue: Wallet operations timeout
**Cause**: Lock contention from concurrent requests
**Solution**: Reduce transaction scope or scale database replicas

### Issue: Ledger entries missing for some transactions
**Cause**: Application failure after balance update but before ledger entry
**Solution**: Ensure `@Transactional` wraps both operations

### Issue: Negative wallet balance observed
**Cause**: Race condition in non-SERIALIZABLE isolation
**Solution**: Verify database is using SERIALIZABLE isolation level

---

## References

- Double-Entry Bookkeeping: https://en.wikipedia.org/wiki/Double-entry_bookkeeping
- Spring Transaction Management: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/annotation/Transactional.html
- ACID Properties: https://en.wikipedia.org/wiki/ACID
- Idempotency in REST APIs: https://tools.ietf.org/html/draft-idempotency-http-header-02

---

**Document Version**: 1.0  
**Last Updated**: February 5, 2026  
**Author**: System Architecture Team
