# Environment Setup Guide

Complete guide for configuring environment variables for the Money Manager Backend.

## Quick Start

### 1. Create Your Environment File

```bash
# Copy the example file
cp .env.example .env
```

### 2. Configure for Your Environment

Edit `.env` with your specific values. See sections below for detailed configuration.

---

## Development Setup (Default)

For local development with H2 in-memory database:

```bash
# .env file
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
```

**That's it!** All other values have sensible defaults for development.

### Running Locally

```bash
# Using Maven
mvn spring-boot:run

# Application available at: http://localhost:8080/api
# H2 Console available at: http://localhost:8080/api/h2-console
```

---

## Production Setup

### Option 1: Docker Deployment (Recommended)

1. **Create `.env` file:**

```bash
cp .env.example .env
```

2. **Configure production values in `.env`:**

```bash
# Profile
SPRING_PROFILES_ACTIVE=prod

# Database (Docker Compose will override host to 'postgres')
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_secure_password_here

# JWT Secret - CRITICAL: Generate a new secret!
JWT_SECRET=<see-generation-instructions-below>

# CORS - Replace with your frontend domain
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Logging
LOG_LEVEL_ROOT=WARN
LOG_LEVEL_APP=INFO
```

3. **Start containers:**

```bash
docker-compose up -d
```

4. **Verify deployment:**

```bash
# Check containers are running
docker-compose ps

# View logs
docker-compose logs -f money-manager-backend

# Test health endpoint
curl http://localhost:8080/api/health
```

### Option 2: Traditional Server Deployment

1. **Create `.env` file with production PostgreSQL settings:**

```bash
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# PostgreSQL Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/MoneyManager
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_secure_db_password

# JPA Configuration
JPA_DDL_AUTO=validate
JPA_DIALECT=org.hibernate.dialect.PostgreSQLDialect

# JWT Secret
JWT_SECRET=<generate-using-command-below>

# CORS
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# Logging
LOG_LEVEL_ROOT=WARN
LOG_LEVEL_APP=INFO
LOG_LEVEL_SPRING_WEB=WARN
LOG_LEVEL_SQL=WARN

# Disable H2 Console
H2_CONSOLE_ENABLED=false
```

2. **Export environment variables (alternative to .env file):**

```bash
# Linux/Mac
export $(cat .env | xargs)

# Or set individually
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/MoneyManager
# ... etc
```

3. **Run application:**

```bash
mvn clean package -DskipTests
java -jar target/money-manager-backend-1.0.0.jar
```

---

## Security Best Practices

### Generating a Secure JWT Secret

**CRITICAL:** Never use the default JWT secret in production!

```bash
# Generate a strong 512-bit secret
openssl rand -base64 64

# Copy the output and set it in your .env file:
# JWT_SECRET=<paste-generated-secret-here>
```

### Environment File Security

```bash
# NEVER commit .env to version control
# Verify .env is in .gitignore
cat .gitignore | grep .env

# Set proper file permissions (Linux/Mac)
chmod 600 .env
```

### Production Checklist

- [ ] Generate new JWT secret using `openssl rand -base64 64`
- [ ] Use strong database password
- [ ] Set `JPA_DDL_AUTO=validate` after initial schema creation
- [ ] Configure CORS with actual frontend domain(s)
- [ ] Set log levels to WARN/ERROR for production
- [ ] Disable H2 console (`H2_CONSOLE_ENABLED=false`)
- [ ] Never commit `.env` file to version control
- [ ] Use environment-specific secrets management (AWS Secrets Manager, Azure Key Vault, etc.)

---

## Environment Variables Reference

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile (dev/prod) | `prod` |
| `JWT_SECRET` | JWT signing secret (min 512 bits) | `<openssl-generated>` |

### Database Variables

| Variable | Description | Default (Dev) | Production Example |
|----------|-------------|---------------|-------------------|
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:h2:mem:testdb` | `jdbc:postgresql://localhost:5432/MoneyManager` |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | JDBC driver | `org.h2.Driver` | `org.postgresql.Driver` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `sa` | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | _(empty)_ | `your_password` |

### Connection Pool Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_MAX_POOL_SIZE` | Maximum connections | `20` |
| `DB_MIN_IDLE` | Minimum idle connections | `5` |
| `DB_CONNECTION_TIMEOUT` | Connection timeout (ms) | `20000` |

### JPA/Hibernate Variables

| Variable | Description | Default | Production |
|----------|-------------|---------|------------|
| `JPA_DDL_AUTO` | Schema generation | `update` | `validate` |
| `JPA_DIALECT` | Hibernate dialect | `H2Dialect` | `PostgreSQLDialect` |
| `JPA_SHOW_SQL` | Log SQL statements | `false` | `false` |

### Security Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_EXPIRATION` | Token expiration (ms) | `86400000` (24h) |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | `604800000` (7d) |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `http://localhost:3000,http://localhost:4200` |
| `CORS_ALLOWED_METHODS` | Allowed HTTP methods | `GET,POST,PUT,DELETE,OPTIONS` |

### Logging Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `LOG_LEVEL_ROOT` | Root logger level | `INFO` |
| `LOG_LEVEL_APP` | Application logger level | `INFO` |
| `LOG_LEVEL_SPRING_WEB` | Spring Web logger level | `INFO` |
| `LOG_LEVEL_SQL` | SQL logger level | `INFO` |

---

## Troubleshooting

### Application won't start

**Check environment variables are loaded:**
```bash
# View current environment
printenv | grep SPRING

# Verify .env file exists
ls -la .env
```

### Database connection failed

**Verify database credentials:**
```bash
# Test PostgreSQL connection
psql -h localhost -U postgres -d MoneyManager

# Check environment variables
echo $SPRING_DATASOURCE_URL
echo $SPRING_DATASOURCE_USERNAME
```

### JWT token errors

**Ensure JWT secret is set:**
```bash
# Check JWT secret is configured
echo $JWT_SECRET | wc -c
# Should be at least 64 characters
```

### Docker Compose issues

**Verify .env file is in the same directory as docker-compose.yml:**
```bash
ls -la .env docker-compose.yml

# Check Docker Compose loads variables
docker-compose config
```

---

## Cloud Platform Deployment

### AWS Elastic Beanstalk

Set environment variables in EB Console or via CLI:

```bash
eb setenv \
  SPRING_PROFILES_ACTIVE=prod \
  SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds.amazonaws.com:5432/MoneyManager \
  SPRING_DATASOURCE_USERNAME=admin \
  SPRING_DATASOURCE_PASSWORD=your_password \
  JWT_SECRET=your_generated_secret \
  CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### Heroku

Set config vars:

```bash
heroku config:set \
  SPRING_PROFILES_ACTIVE=prod \
  JWT_SECRET=your_generated_secret \
  CORS_ALLOWED_ORIGINS=https://yourdomain.com \
  --app money-manager-backend
```

### Azure App Service

Set application settings in Azure Portal or via CLI:

```bash
az webapp config appsettings set \
  --resource-group myResourceGroup \
  --name money-manager-backend \
  --settings \
    SPRING_PROFILES_ACTIVE=prod \
    JWT_SECRET=your_generated_secret \
    CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

---

## Additional Resources

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [12-Factor App Configuration](https://12factor.net/config)

---

**Last Updated:** February 2026  
**Version:** 2.0.0
