# Architecture & Design Patterns - Money Manager Backend

## 🏗️ System Architecture

### Layered Architecture Pattern

The application follows a clean, layered architecture separating concerns:

```
┌─────────────────────────────────────────┐
│         Client Layer (REST API)         │
│  (Postman, Web Client, Mobile App)      │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      Controller Layer (API Gateway)     │
│  - Request validation                   │
│  - Response formatting                  │
│  - HTTP handling                        │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      Service Layer (Business Logic)     │
│  - Transaction processing               │
│  - User management                      │
│  - Budget calculations                  │
│  - Authentication                       │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│     Repository Layer (Data Access)      │
│  - Database queries (JPA)                │
│  - Data persistence                     │
│  - Query optimization                   │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│        Database Layer (PostgreSQL/H2)   │
│  - Data storage                         │
│  - Transactions                         │
│  - Indexing                             │
└─────────────────────────────────────────┘
```

### Cross-Cutting Concerns

```
┌──────────────────────────────────────────┐
│   Security (Spring Security + JWT)       │
│   Logging (SLF4J + Logback)              │
│   Exception Handling (Global Handler)    │
│   Validation (Jakarta Bean Validation)   │
│   CORS (Cross-Origin Resource Sharing)   │
└──────────────────────────────────────────┘
```

---

## 📐 Design Patterns Used

### 1. **Dependency Injection**
```java
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    // Constructor injection
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
}
```
**Benefits**: Loose coupling, testability, flexibility

### 2. **Repository Pattern**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```
**Benefits**: Abstraction of data access, easy testing with mocks

### 3. **Service Layer Pattern**
```java
@Service
public class TransactionService {
    // Business logic separated from controllers
}
```
**Benefits**: Reusability, testability, single responsibility

### 4. **DTO (Data Transfer Object) Pattern**
```java
public class TransactionDTO {
    private String type;
    private String category;
    private BigDecimal amount;
}
```
**Benefits**: API contract stability, security (hiding entities)

### 5. **Global Exception Handler Pattern**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(...) {
        // Centralized error handling
    }
}
```
**Benefits**: Consistent error responses, centralized error logic

### 6. **Builder Pattern**
```java
User user = User.builder()
    .firstName("John")
    .lastName("Doe")
    .email("john@example.com")
    .build();
```
**Benefits**: Readable object construction, immutability

### 7. **Factory Pattern**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
**Benefits**: Centralized object creation, easy to swap implementations

### 8. **Singleton Pattern**
```java
@Service  // Implicitly singleton by Spring
public class AuthService { }
```
**Benefits**: Single instance management, resource efficiency

### 9. **Strategy Pattern**
```java
public interface PasswordEncoder {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
// BCryptPasswordEncoder is one strategy
```
**Benefits**: Pluggable algorithms, flexibility

---

## 🔐 Security Architecture

### JWT Authentication Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ 1. POST /auth/login
       │    {email, password}
       │
       ▼
┌──────────────────────────────┐
│  AuthController              │
│  - Validate credentials      │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  AuthService                 │
│  - Check password            │
│  - Generate JWT token        │
└──────────────┬───────────────┘
               │
       ┌───────┴────────┐
       │                │
       ▼                ▼
  Token 1: Access    Token 2: Refresh
  24 hours           7 days
       │                │
       └────────┬───────┘
                │
        2. Response with tokens
                │
       ▼────────────────────────┐
       │ Client stores tokens   │
       │ Authorization: Bearer <token>
       │
       │ 3. GET /api/users/profile
       │    Headers: Authorization: Bearer <access_token>
       │
       ▼
┌──────────────────────────────┐
│  JwtAuthenticationFilter     │
│  - Extract token             │
│  - Validate signature        │
│  - Check expiration          │
│  - Load UserDetails          │
│  - Set SecurityContext       │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  UserController              │
│  - Access authenticated user │
│  - Return profile data       │
└──────────────────────────────┘
```

### Password Hashing
```
User Input: "Test@1234"
       │
       ▼
BCryptPasswordEncoder
  - Add salt
  - Hash multiple iterations (10+)
  - Generate secure hash
       │
       ▼
Stored in DB: "$2a$10$slkdjflksdjf..."
```

---

## 🗄️ Database Design

### Entity Relationship Diagram

```
┌──────────────────────┐
│       Users          │
├──────────────────────┤
│ id (PK)              │
│ firstName            │
│ lastName             │
│ email (UNIQUE)       │
│ password             │
│ active               │
│ created_at           │
│ updated_at           │
└──────┬───────────────┘
       │
       │ 1:N relationship
       │
       ├─────────────────────────┬─────────────────────────┐
       │                         │                         │
       ▼                         ▼                         ▼
┌──────────────────────┐  ┌──────────────────────┐
│   Transactions       │  │     Budgets          │
├──────────────────────┤  ├──────────────────────┤
│ id (PK)              │  │ id (PK)              │
│ user_id (FK)         │  │ user_id (FK)         │
│ type (INCOME/EXPENSE)│  │ category             │
│ category             │  │ limit                │
│ amount               │  │ month_year           │
│ description          │  │ active               │
│ transaction_date     │  │ created_at           │
│ created_at           │  │ updated_at           │
│ updated_at           │  │                      │
└──────────────────────┘  └──────────────────────┘
```

### Normalization
- **1NF**: All attributes are atomic
- **2NF**: No partial dependencies
- **3NF**: No transitive dependencies

### Indexing Strategy
```sql
-- Performance optimization indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_month_year ON budgets(month_year);
```

---

## 📊 Request/Response Flow

### Standard Request/Response Cycle

```
HTTP Request
    │
    ▼
┌──────────────────────────┐
│  CORS Filter             │
│  - Check allowed origins │
└──────────────┬───────────┘
               │
               ▼
┌──────────────────────────┐
│  JWT Filter              │
│  - Validate token        │
│  - Set authentication    │
└──────────────┬───────────┘
               │
               ▼
┌──────────────────────────┐
│  Controller              │
│  - Route request         │
│  - Validate input        │
└──────────────┬───────────┘
               │
               ▼
┌──────────────────────────┐
│  Service                 │
│  - Business logic        │
│  - Database operations   │
└──────────────┬───────────┘
               │
               ▼
┌──────────────────────────┐
│  Response Building       │
│  - Wrap in ApiResponse   │
│  - Set status code       │
└──────────────┬───────────┘
               │
               ▼
┌──────────────────────────┐
│  Exception Handler       │
│  (if error occurred)     │
└──────────────┬───────────┘
               │
               ▼
         HTTP Response
```

### Standard Response Format

```json
{
  "status": 200,
  "message": "Success message",
  "data": { /* actual data */ },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 🔄 Pagination Architecture

```
Client Request
  ├─ page: 0        (first page)
  ├─ size: 20       (20 items per page)
  ├─ sortBy: date
  └─ direction: DESC

         │
         ▼

┌────────────────────────┐
│  Repository            │
│  .findByUserId(userId, │
│   PageRequest.of(...)) │
└────────┬───────────────┘
         │
         ▼

         Database
         - Calculate offset: 0
         - Fetch 20 records
         - Calculate total count

         │
         ▼

┌────────────────────────┐
│  Page<T> Response      │
│  - content: [...]      │
│  - totalElements: 100  │
│  - totalPages: 5       │
│  - number: 0           │
│  - empty: false        │
└────────────────────────┘
```

---

## 🧪 Testing Architecture

### Unit Test Pattern

```
Test Class
    │
    ├─ Mock Dependencies
    │  └─ @Mock private UserRepository
    │  └─ @Mock private PasswordEncoder
    │
    ├─ Create Component Under Test
    │  └─ @InjectMocks private AuthService
    │
    └─ Test Methods
       ├─ Setup (given)
       │  └─ when(userRepository.save(...))
       │     .thenReturn(user)
       │
       ├─ Execute (when)
       │  └─ authService.register(request)
       │
       └─ Verify (then)
          └─ assertNotNull(response)
          └─ verify(userRepository).save(...)
```

---

## 📈 Performance Considerations

### Caching Strategy (Future Enhancement)
```java
@Cacheable(value = "users", key = "#email")
public User getUserByEmail(String email) {
    return userRepository.findByEmail(email);
}
```

### Connection Pooling
```yaml
datasource:
  hikari:
    maximum-pool-size: 20
    minimum-idle: 5
    connection-timeout: 20000
```

### Database Query Optimization
- Use pagination for large datasets
- Create indexes on frequently queried columns
- Use JPA projections for read-only queries
- Lazy load relationships when possible

---

## 🔄 Transaction Management

### Service Layer Transactions
```java
@Service
public class TransactionService {
    
    @Transactional  // Begin transaction
    public TransactionDTO createTransaction(...) {
        // All operations in single transaction
        // Automatic rollback on exception
        // Auto-commit on success
    }
}
```

### Read-Only Transactions
```java
@Transactional(readOnly = true)
public List<TransactionDTO> getTransactions(...) {
    // Optimized for read operations
    // No dirty checks
    // Better performance
}
```

---

## 🚀 Scalability Architecture

### Horizontal Scaling Pattern

```
┌─────────────────────────────┐
│      Load Balancer          │
│    (Nginx/AWS ALB)          │
└────────┬────────────────────┘
         │
    ┌────┼────┬─────┐
    │    │    │     │
    ▼    ▼    ▼     ▼
┌────┐┌────┐┌────┐┌────┐
│ App│ │App │ │App │ │App │ (Multiple instances)
│ #1 │ │ #2 │ │ #3 │ │ #4 │
└──┬─┘└──┬─┘└──┬─┘└──┬─┘
   │     │     │     │
   └─────┼─────┼─────┘
         │
    ┌────▼─────┐
    │ Shared   │
    │ Database │  (PostgreSQL with replication)
    └──────────┘
    
    └─────────────────┐
         Cache (Redis) │ (Optional future addition)
         └─────────────┘
```

---

## 📋 Code Organization Principles

### Single Responsibility Principle (SRP)
- Each class has one reason to change
- Controllers handle HTTP
- Services handle business logic
- Repositories handle data access

### Open/Closed Principle (OCP)
- Entities open for extension, closed for modification
- Use interfaces for multiple implementations

### Liskov Substitution Principle (LSP)
- Repository interface can be implemented by different databases

### Interface Segregation Principle (ISP)
- Small, focused interfaces
- UserRepository only deals with User operations

### Dependency Inversion Principle (DIP)
- Depend on abstractions, not concrete implementations
- Inject interfaces, not implementations

---

## 🔒 Security Layers

```
┌──────────────────────────┐
│  Application Layer       │
│  - Input validation      │
│  - Business rules        │
└────────────┬─────────────┘
             │
┌────────────▼──────────────┐
│  Authentication           │
│  - JWT verification       │
│  - User identification    │
└────────────┬──────────────┘
             │
┌────────────▼──────────────┐
│  Authorization            │
│  - Role checking          │
│  - Permission validation  │
└────────────┬──────────────┘
             │
┌────────────▼──────────────┐
│  Data Layer               │
│  - User isolation         │
│  - SQL parameterization   │
└──────────────────────────┘
```

---

## 📚 Design Pattern Summary

| Pattern | Usage | Benefits |
|---------|-------|----------|
| **Layered** | Overall architecture | Separation of concerns |
| **Dependency Injection** | Spring autowiring | Loose coupling |
| **Repository** | Data access | Easy testing, abstraction |
| **Service** | Business logic | Reusability |
| **DTO** | API contracts | Security, stability |
| **Builder** | Object creation | Readability |
| **Factory** | Bean creation | Flexibility |
| **Singleton** | Spring beans | Resource efficiency |
| **Strategy** | Password encoding | Pluggability |
| **Global Exception Handler** | Error handling | Consistency |

---

**Architecture Version**: 1.0  
**Last Updated**: January 2024
