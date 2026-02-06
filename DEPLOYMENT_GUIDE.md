# Deployment Guide - Money Manager Backend

## Quick Start

### 1. Development Environment (Local)

#### Prerequisites
- Java 17+
- Maven 3.6+
- Git

#### Steps
```bash
# Clone repository
git clone <repository-url>
cd money-manager-backend

# Copy environment template
cp .env.example .env

# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Application will be available at http://localhost:8080
```

---

## 2. Production Deployment

### Option A: Docker (Recommended)

#### Prerequisites
- Docker 20.10+
- Docker Compose 1.29+

#### Steps

1. **Create `.env` file with production settings:**
```env
SPRING_PROFILE=prod
SERVER_PORT=8080
DB_URL=jdbc:postgresql://postgres:5432/money_manager
DB_DRIVER=org.postgresql.Driver
DB_USERNAME=postgres
DB_PASSWORD=your_strong_password_here
DB_MAX_POOL_SIZE=20
JPA_DDL_AUTO=validate
JWT_SECRET=your-very-long-secret-key-min-256-bits-change-this-in-prod
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
LOG_LEVEL=WARN
```

2. **Build Docker image:**
```bash
mvn clean package -DskipTests
docker build -t money-manager-backend:1.0.0 .
```

3. **Run with Docker Compose:**
```bash
docker-compose up -d
```

4. **Verify containers are running:**
```bash
docker-compose ps
```

5. **View logs:**
```bash
docker-compose logs -f money-manager-backend
```

---

### Option B: Traditional Server Deployment (Ubuntu 20.04+)

#### Prerequisites
- Ubuntu 20.04 LTS or later
- Java 17 installed
- PostgreSQL 12+ installed
- Nginx (optional, for reverse proxy)

#### Steps

1. **Install Java 17:**
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk

# Verify
java -version
```

2. **Install and configure PostgreSQL:**
```bash
sudo apt install -y postgresql postgresql-contrib

# Create database
sudo -u postgres createdb money_manager

# Create user
sudo -u postgres psql -c "CREATE USER app_user WITH PASSWORD 'strong_password';"
sudo -u postgres psql -c "ALTER ROLE app_user WITH SUPERUSER;"
```

3. **Clone and build application:**
```bash
cd /opt
sudo git clone <repository-url> money-manager-backend
cd money-manager-backend
sudo chown -R $USER:$USER .
```

4. **Create `.env` file:**
```bash
cat > .env << EOF
SPRING_PROFILE=prod
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/money_manager
DB_USERNAME=app_user
DB_PASSWORD=strong_password
JWT_SECRET=your-very-long-secret-key-minimum-256-bits
CORS_ALLOWED_ORIGINS=https://yourdomain.com
LOG_LEVEL=WARN
EOF
```

5. **Build application:**
```bash
mvn clean package -DskipTests
```

6. **Create systemd service:**
```bash
sudo tee /etc/systemd/system/money-manager.service > /dev/null << EOF
[Unit]
Description=Money Manager Backend Application
After=network.target postgresql.service

[Service]
Type=simple
User=app_user
WorkingDirectory=/opt/money-manager-backend
EnvironmentFile=/opt/money-manager-backend/.env
ExecStart=/usr/bin/java -jar target/money-manager-backend-1.0.0.jar
Restart=on-failure
RestartSec=10s
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF
```

7. **Create app user:**
```bash
sudo useradd -r -s /bin/bash app_user
sudo chown -R app_user:app_user /opt/money-manager-backend
```

8. **Enable and start service:**
```bash
sudo systemctl daemon-reload
sudo systemctl enable money-manager
sudo systemctl start money-manager

# Check status
sudo systemctl status money-manager
```

9. **Configure Nginx (Optional):**
```bash
sudo tee /etc/nginx/sites-available/money-manager > /dev/null << EOF
server {
    listen 80;
    server_name yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# Enable site
sudo ln -s /etc/nginx/sites-available/money-manager /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

10. **Setup SSL with Certbot:**
```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```

---

## 3. Cloud Platform Deployment

### AWS Elastic Beanstalk

1. **Install EB CLI:**
```bash
pip install awsebcli --upgrade --user
```

2. **Initialize EB:**
```bash
eb init -p "Docker running on 64bit Amazon Linux 2" money-manager-backend
```

3. **Create environment:**
```bash
eb create production-env
```

4. **Set environment variables:**
```bash
eb setenv \
  SPRING_PROFILE=prod \
  DB_URL=jdbc:postgresql://your-rds-endpoint/money_manager \
  DB_USERNAME=admin \
  DB_PASSWORD=your_password \
  JWT_SECRET=your-secret-key \
  CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

5. **Deploy:**
```bash
eb deploy
```

---

### Heroku

1. **Install Heroku CLI:**
```bash
curl https://cli-assets.heroku.com/install.sh | sh
```

2. **Login:**
```bash
heroku login
```

3. **Create app:**
```bash
heroku create money-manager-backend
```

4. **Add PostgreSQL addon:**
```bash
heroku addons:create heroku-postgresql:standard-0 --app money-manager-backend
```

5. **Set config variables:**
```bash
heroku config:set \
  SPRING_PROFILE=prod \
  JWT_SECRET=your-secret-key \
  CORS_ALLOWED_ORIGINS=https://yourdomain.com \
  --app money-manager-backend
```

6. **Deploy:**
```bash
git push heroku main
```

---

## 4. Health Checks and Monitoring

### Health Endpoint
```bash
curl http://localhost:8080/api/health
```

### Monitoring Setup

#### Using Prometheus and Grafana:

1. **Enable Actuator metrics:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

2. **Add Prometheus dependency:**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

3. **Access metrics:**
```
http://localhost:8080/actuator/prometheus
```

---

## 5. Backup and Recovery

### Database Backup (PostgreSQL)

```bash
# Full backup
pg_dump -U app_user -h localhost money_manager > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore
psql -U app_user -h localhost money_manager < backup_20240115_120000.sql
```

### Automated Backups with Cron:

```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * pg_dump -U app_user -h localhost money_manager | gzip > /backups/db_$(date +\%Y\%m\%d).sql.gz
```

---

## 6. Scaling and Performance

### Horizontal Scaling (Docker Swarm/Kubernetes)

#### Docker Swarm:
```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.yml money-manager

# Scale service
docker service scale money-manager_money-manager-backend=3
```

#### Kubernetes:
```bash
# Create deployment
kubectl create deployment money-manager --image=money-manager-backend:1.0.0

# Scale
kubectl scale deployment money-manager --replicas=3

# Expose service
kubectl expose deployment money-manager --port=80 --target-port=8080 --type=LoadBalancer
```

---

## 7. Security Hardening

### Before Production Deployment

1. **Change JWT Secret:**
```bash
# Generate strong secret
openssl rand -base64 256
```

2. **Configure HTTPS:**
- Obtain SSL certificate (Let's Encrypt)
- Configure in Nginx or application properties

3. **Set up firewall:**
```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

4. **Disable H2 Console in production:**
```yaml
spring:
  h2:
    console:
      enabled: false
```

5. **Enable HTTPS only:**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

6. **Configure security headers:**
```yaml
server:
  servlet:
    session:
      cookie:
        http-only: true
        secure: true
```

---

## 8. Troubleshooting

### Application won't start
```bash
# Check logs
docker-compose logs money-manager-backend

# Or for systemd
sudo journalctl -u money-manager -f
```

### Database connection issues
```bash
# Test PostgreSQL connection
psql -h localhost -U app_user -d money_manager

# Check credentials in .env
```

### High memory usage
```bash
# Adjust JVM settings
export JAVA_OPTS="-Xmx512m -Xms256m"
```

### Slow API responses
```bash
# Check database indexes
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_transaction_user_id ON transactions(user_id);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_budget_user_id ON budgets(user_id);
```

---

## 9. Version Updates

### Update to Latest Version

```bash
# Stop service
sudo systemctl stop money-manager

# Pull latest code
git pull origin main

# Rebuild
mvn clean package -DskipTests

# Start service
sudo systemctl start money-manager
```

---

## 10. Rollback Procedure

```bash
# Stop current version
sudo systemctl stop money-manager

# Restore previous JAR
cp /opt/backups/money-manager-backend-0.9.9.jar target/

# Start service
sudo systemctl start money-manager

# Verify
curl http://localhost:8080/api/health
```

---

## Support and Maintenance

- Check logs regularly
- Monitor database growth
- Update dependencies monthly
- Test backups quarterly
- Review security advisories

---

**Last Updated**: January 2024  
**Version**: 1.0.0
