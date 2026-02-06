# Getting Started - Money Manager Backend

Welcome! This guide will help you get the Money Manager Backend application up and running in minutes.

## 📋 Prerequisites

Before starting, ensure you have:

- **Java 17 or higher** 
  ```bash
  java -version
  ```
  
- **Maven 3.6 or higher**
  ```bash
  mvn -version
  ```

- **Git** (optional, for cloning)

## 🚀 Quick Start (5 Minutes)

### Step 1: Navigate to Project Directory
```bash
cd "Money Manager"
```

### Step 2: Copy Environment Configuration
```bash
# On Windows
copy .env.example .env

# On macOS/Linux
cp .env.example .env
```

The `.env` file contains default values for development. No changes needed to run locally with H2 in-memory database.

### Step 3: Build the Project
```bash
mvn clean install
```

This will:
- Download dependencies
- Compile source code
- Run unit tests
- Package the application

### Step 4: Run the Application
```bash
mvn spring-boot:run
```

Or alternatively:
```bash
java -jar target/money-manager-backend-1.0.0.jar
```

### Step 5: Verify It's Running
```bash
curl http://localhost:8080/api/health
```

Expected response:
```json
{
  "status": 200,
  "message": "Application is running successfully",
  "data": "HEALTHY",
  "timestamp": "2024-01-15T10:30:00"
}
```

✅ **Your application is now running on http://localhost:8080**

---

## 📖 Using the API

### 1. Create a User Account
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "Test@1234",
    "confirmPassword": "Test@1234"
  }'
```

Response includes `accessToken` - save this for authenticated requests.

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Test@1234"
  }'
```

### 3. Get Your Profile (Authenticated)
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Replace `YOUR_ACCESS_TOKEN` with the token from registration/login response.

### 4. Create a Transaction
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "type": "EXPENSE",
    "category": "Food & Dining",
    "amount": 45.50,
    "description": "Lunch",
    "transactionDate": "2024-01-15T12:30:00"
  }'
```

### 5. View Transactions
```bash
curl -X GET "http://localhost:8080/api/transactions?page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 🔧 Configuration

### Default Development Settings
- **Server Port**: 8080
- **Database**: H2 (in-memory)
- **Log Level**: INFO
- **JWT Expiration**: 24 hours

### Changing Settings

Edit `.env` file to customize:

```env
# Server
SERVER_PORT=8080

# Database (H2 default)
DB_URL=jdbc:h2:mem:testdb
DB_DRIVER=org.h2.Driver
DB_USERNAME=sa
DB_PASSWORD=

# JWT
JWT_SECRET=your-secret-key-change-in-production-at-least-256-bits
JWT_EXPIRATION=86400000

# Logging
LOG_LEVEL=INFO
LOG_FILE_PATH=logs/app.log
```

Then restart the application for changes to take effect.

---

## 🐳 Docker Setup (Alternative)

If you prefer to use Docker:

### Prerequisites
- Docker 20.10+
- Docker Compose 1.29+

### Start with Docker Compose
```bash
docker-compose up -d
```

This will:
- Start a PostgreSQL database
- Start the Money Manager Backend
- Create all necessary tables

### Access the Application
```bash
curl http://localhost:8080/api/health
```

### View Logs
```bash
docker-compose logs -f money-manager-backend
```

### Stop Services
```bash
docker-compose down
```

---

## 📂 Project Structure Overview

```
Money Manager/
├── src/main/java/          # Source code
├── src/test/java/          # Tests
├── pom.xml                 # Maven configuration
├── README.md               # Full documentation
├── API_DOCUMENTATION.md    # API reference
└── DEPLOYMENT_GUIDE.md     # Production deployment
```

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn test jacoco:report
```

---

## 📚 Documentation Files

1. **README.md** - Complete project documentation
2. **API_DOCUMENTATION.md** - Detailed API endpoints and examples
3. **DEPLOYMENT_GUIDE.md** - Production deployment instructions
4. **PROJECT_SUMMARY.md** - Feature overview and architecture

Read these for:
- API endpoint reference
- Environment variable details
- Security configuration
- Deployment options

---

## 🔑 Key Features

✅ User Registration & Login (JWT)  
✅ Transaction Management (CRUD)  
✅ Budget Management  
✅ User Profile Management  
✅ Comprehensive Error Handling  
✅ Input Validation  
✅ Database Support (H2 & PostgreSQL)  
✅ Docker Ready  
✅ Unit Tests  
✅ Production Ready  

---

## ⚡ Common Commands

```bash
# Clean and build
mvn clean install

# Run application
mvn spring-boot:run

# Run tests only
mvn test

# Build JAR without tests
mvn clean package -DskipTests

# View help
mvn help:active-profiles

# Check dependencies
mvn dependency:tree
```

---

## 🆘 Troubleshooting

### "Port 8080 is already in use"
```bash
# Change port in .env
SERVER_PORT=8081

# Or kill the process using port 8080
# Windows: netstat -ano | findstr :8080
# Linux: lsof -i :8080
```

### "Maven not recognized"
Ensure Maven is installed and in your PATH:
```bash
mvn -version
```

### "Java version error"
Ensure Java 17+ is installed:
```bash
java -version
```

### Database connection issues
```bash
# Check database settings in .env
# Default H2 should work without any changes
# For PostgreSQL, ensure it's running and credentials are correct
```

### Tests failing
```bash
# Run tests in verbose mode
mvn test -X

# Skip tests during build
mvn clean install -DskipTests
```

---

## 📱 Sample Request/Response

### Register
**Request:**
```
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "Test@1234",
  "confirmPassword": "Test@1234"
}
```

**Response (201):**
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

## 🔐 Security Notes

1. The default JWT secret is for development only. **Change it in production!**
2. Never commit `.env` file with production secrets
3. Use HTTPS in production
4. Change database credentials from defaults
5. Enable CORS only for trusted origins

---

## 📞 Next Steps

1. **Explore the API**: See [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
2. **Deploy to Production**: See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
3. **Review Architecture**: See [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)
4. **Read Full Documentation**: See [README.md](README.md)

---

## 💡 Tips

- Use Postman or Insomnia to test API endpoints
- Keep your JWT token safe (contains user information)
- Check logs for debugging: `logs/app.log`
- Use `page` and `size` parameters for pagination
- Always include `Authorization` header for authenticated endpoints

---

## ✅ Checklist

- [ ] Java 17+ installed
- [ ] Maven installed
- [ ] Project cloned/downloaded
- [ ] `.env` file copied
- [ ] `mvn clean install` successful
- [ ] Application running on port 8080
- [ ] Health check passing
- [ ] First user registered
- [ ] API working with authentication

**All done? 🎉 You're ready to use Money Manager Backend!**

---

**Version**: 1.0.0  
**Last Updated**: January 2024
