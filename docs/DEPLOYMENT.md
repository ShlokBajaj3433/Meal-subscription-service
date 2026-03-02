# Deployment Guide

Complete guide for deploying the Meal Subscription Service to production.

## Table of Contents

- [Deployment Options](#deployment-options)
- [Prerequisites](#prerequisites)
- [Environment Configuration](#environment-configuration)
- [Docker Deployment](#docker-deployment)
- [Cloud Deployment](#cloud-deployment)
- [Database Setup](#database-setup)
- [Security Configuration](#security-configuration)
- [Monitoring & Health Checks](#monitoring--health-checks)
- [Backup & Recovery](#backup--recovery)
- [Troubleshooting](#troubleshooting)

## Deployment Options

| Option | Difficulty | Cost | Scalability | Recommended For |
|--------|-----------|------|-------------|-----------------|
| **Docker Compose** | Easy | Low | Limited | Development, small deployments |
| **AWS ECS** | Medium | Medium | High | Production, auto-scaling |
| **Kubernetes** | Hard | Medium-High | Very High | Large-scale production |
| **Heroku** | Easy | Medium | Medium | Quick prototypes |
| **Traditional VPS** | Medium | Low-Medium | Medium | Budget-conscious production |

## Prerequisites

### Required

- **Domain name** with SSL certificate
- **PostgreSQL 16+** database
- **SMTP server** for email notifications (optional)
- **Stripe account** with API keys
- **Server/Cloud instance** with:
  - 2+ GB RAM
  - 2+ CPU cores
  - 20+ GB storage
  - Docker support (if using containers)

### Recommended

- **CDN** (Cloudflare, CloudFront) for static assets
- **Load balancer** for high availability
- **Monitoring service** (Datadog, New Relic, Prometheus)
- **Log aggregation** (ELK stack, CloudWatch)
- **Backup solution** for database

## Environment Configuration

### Required Environment Variables

Create a `.env` file in production (never commit to version control):

```env
# ─── Application ──────────────────────────────────────────────────────────────
SPRING_PROFILES_ACTIVE=prod

# ─── Database ─────────────────────────────────────────────────────────────────
SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/mealdb_prod
SPRING_DATASOURCE_USERNAME=meal_user
SPRING_DATASOURCE_PASSWORD=<STRONG_PASSWORD_HERE>

# Connection pool tuning for production
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=20
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=30000
SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=600000
SPRING_DATASOURCE_HIKARI_MAX_LIFETIME=1800000

# ─── JWT Security ─────────────────────────────────────────────────────────────
# Generate with: openssl rand -base64 64
JWT_SECRET=<SECURE_RANDOM_64_CHAR_BASE64_STRING>
JWT_EXPIRATION_MS=86400000  # 24 hours

# ─── Stripe ───────────────────────────────────────────────────────────────────
STRIPE_SECRET_KEY=sk_live_<YOUR_LIVE_KEY>
STRIPE_WEBHOOK_SECRET=whsec_<YOUR_WEBHOOK_SECRET>

# ─── Email (Production SMTP) ──────────────────────────────────────────────────
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=<SENDGRID_API_KEY>
MAIL_FROM=noreply@yourdomain.com

# ─── CORS ─────────────────────────────────────────────────────────────────────
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# ─── JVM Options ──────────────────────────────────────────────────────────────
JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### Generating Secure Secrets

```bash
# JWT Secret (64 bytes, base64 encoded)
openssl rand -base64 64

# Database Password (32 characters)
openssl rand -base64 32

# General Random String
openssl rand -hex 32
```

### Production Application Configuration

Create `application-prod.yml`:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    hikari:
      maximum-pool-size: ${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE:20}
      minimum-idle: ${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE:5}
      connection-timeout: ${SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT:30000}

  jpa:
    show-sql: false  # Disable SQL logging in production
    hibernate:
      ddl-auto: validate  # Never auto-create schema in production
    properties:
      hibernate:
        format_sql: false
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

  flyway:
    enabled: true
    locations: classpath:db/migration  # No test data in production
    validate-on-migrate: true
    baseline-on-migrate: false

  thymeleaf:
    cache: true  # Enable template caching

logging:
  level:
    root: INFO
    com.mealsubscription: INFO
    org.springframework.security: WARN
    org.hibernate: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: /var/log/meal-service/application.log
    max-size: 100MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
```

## Docker Deployment

### Step 1: Build the Application

```bash
# Build JAR
mvn clean package -DskipTests

# Verify JAR
ls -lh meal-service/target/meal-service-*.jar
```

### Step 2: Build Docker Image

```bash
# Build image
docker build -t meal-subscription-service:1.0.0 .

# Tag for registry (example: Docker Hub)
docker tag meal-subscription-service:1.0.0 yourusername/meal-subscription-service:1.0.0
docker tag meal-subscription-service:1.0.0 yourusername/meal-subscription-service:latest

# Push to registry
docker push yourusername/meal-subscription-service:1.0.0
docker push yourusername/meal-subscription-service:latest
```

### Step 3: Production Docker Compose

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  app:
    image: yourusername/meal-subscription-service:1.0.0
    restart: always
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/mealdb_prod
      - SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - STRIPE_SECRET_KEY=${STRIPE_SECRET_KEY}
      - STRIPE_WEBHOOK_SECRET=${STRIPE_WEBHOOK_SECRET}
      - MAIL_HOST=${MAIL_HOST}
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
      - CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS}
    depends_on:
      db:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - app-network
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  db:
    image: postgres:16-alpine
    restart: always
    environment:
      - POSTGRES_DB=mealdb_prod
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
      - POSTGRES_INITDB_ARGS=--encoding=UTF-8 --lc-collate=C --lc-ctype=C
    volumes:
      - pgdata_prod:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME} -d mealdb_prod"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - app-network
    # Don't expose port 5432 to host in production (only via network)

  nginx:
    image: nginx:alpine
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - app
    networks:
      - app-network

volumes:
  pgdata_prod:
    driver: local

networks:
  app-network:
    driver: bridge
```

### Step 4: Nginx Reverse Proxy

Create `nginx.conf`:

```nginx
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server app:8080;
    }

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=auth_limit:10m rate=5r/m;

    server {
        listen 80;
        server_name yourdomain.com www.yourdomain.com;
        
        # Redirect HTTP to HTTPS
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name yourdomain.com www.yourdomain.com;

        # SSL Configuration
        ssl_certificate /etc/nginx/ssl/fullchain.pem;
        ssl_certificate_key /etc/nginx/ssl/privkey.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;
        ssl_prefer_server_ciphers on;

        # Security Headers
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
        add_header X-Frame-Options "DENY" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;

        # Gzip Compression
        gzip on;
        gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

        # API endpoints with rate limiting
        location /api/v1/auth {
            limit_req zone=auth_limit burst=5 nodelay;
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location /api {
            limit_req zone=api_limit burst=20 nodelay;
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Static content
        location /css {
            proxy_pass http://backend;
            proxy_cache_valid 200 1d;
        }

        location /js {
            proxy_pass http://backend;
            proxy_cache_valid 200 1d;
        }

        # Default proxy
        location / {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # WebSocket support (if needed later)
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
        }

        # Health check endpoint (no rate limit)
        location /actuator/health {
            proxy_pass http://backend;
            access_log off;
        }
    }
}
```

### Step 5: Deploy

```bash
# Load environment variables
source .env

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Check logs
docker-compose -f docker-compose.prod.yml logs -f app

# Verify health
curl https://yourdomain.com/actuator/health
```

## Cloud Deployment

### AWS ECS (Elastic Container Service)

#### Prerequisites

- AWS CLI configured
- ECR repository created
- ECS cluster created
- RDS PostgreSQL instance running

#### Push to ECR

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin 123456789.dkr.ecr.us-east-1.amazonaws.com

# Tag image
docker tag meal-subscription-service:1.0.0 \
  123456789.dkr.ecr.us-east-1.amazonaws.com/meal-subscription-service:1.0.0

# Push
docker push 123456789.dkr.ecr.us-east-1.amazonaws.com/meal-subscription-service:1.0.0
```

#### ECS Task Definition

```json
{
  "family": "meal-subscription-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "containerDefinitions": [
    {
      "name": "app",
      "image": "123456789.dkr.ecr.us-east-1.amazonaws.com/meal-subscription-service:1.0.0",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:postgresql://rds-endpoint:5432/mealdb"
        }
      ],
      "secrets": [
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:jwt-secret"
        },
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:db-password"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/meal-subscription-service",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

### Heroku

```bash
# Login
heroku login

# Create app
heroku create meal-subscription-service-prod

# Add PostgreSQL
heroku addons:create heroku-postgresql:standard-0

# Set environment variables
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set JWT_SECRET=<your-secret>
heroku config:set STRIPE_SECRET_KEY=<your-key>

# Deploy
git push heroku main

# Check logs
heroku logs --tail
```

## Database Setup

### PostgreSQL Production Configuration

```sql
-- Create database
CREATE DATABASE mealdb_prod
    WITH ENCODING='UTF8'
    LC_COLLATE='en_US.UTF-8'
    LC_CTYPE='en_US.UTF-8'
    TEMPLATE=template0;

-- Create user
CREATE USER meal_user WITH ENCRYPTED PASSWORD '<strong-password>';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE mealdb_prod TO meal_user;

-- Connect to database
\c mealdb_prod

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO meal_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO meal_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO meal_user;

-- Performance tuning (adjust based on your resources)
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;
ALTER SYSTEM SET work_mem = '4MB';
ALTER SYSTEM SET max_connections = 100;

-- Reload configuration
SELECT pg_reload_conf();
```

### Flyway Migration

Migrations run automatically on application startup. To run manually:

```bash
# Validate migrations
mvn flyway:validate -Dflyway.url=jdbc:postgresql://prod-host:5432/mealdb_prod

# Run migrations
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://prod-host:5432/mealdb_prod

# Check status
mvn flyway:info -Dflyway.url=jdbc:postgresql://prod-host:5432/mealdb_prod
```

## Security Configuration

### Production Security Checklist

- [ ] **Strong JWT Secret**: 64+ character random base64 string
- [ ] **Database Credentials**: Strong passwords, not default
- [ ] **HTTPS Only**: SSL/TLS certificates installed and enforced
- [ ] **Firewall Rules**: Only ports 80/443 exposed to internet
- [ ] **Database Access**: Not publicly accessible, only from app servers
- [ ] **Environment Variables**: Secrets in environment, not in code
- [ ] **CORS Configuration**: Only trusted origins
- [ ] **Rate Limiting**: Configured at Nginx/API Gateway level
- [ ] **Security Headers**: CSP, HSTS, X-Frame-Options configured
- [ ] **Stripe Webhooks**: Signature verification enabled
- [ ] **Actuator Endpoints**: Secured or disabled in production
- [ ] **SQL Injection**: Using parameterized queries (JPA handles this)
- [ ] **Dependencies**: No known vulnerabilities (`mvn dependency-check:check`)
- [ ] **Regular Updates**: Security patches applied promptly

### SSL Certificate Setup (Let's Encrypt)

```bash
# Install Certbot
sudo apt-get update
sudo apt-get install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Auto-renewal (cron)
sudo certbot renew --dry-run
```

## Monitoring & Health Checks

### Health Endpoints

```bash
# Application health
curl https://yourdomain.com/actuator/health

# Expected response:
# {"status":"UP","groups":["liveness","readiness"]}

# Detailed health (requires authentication)
curl -H "Authorization: Bearer <admin-token>" \
  https://yourdomain.com/actuator/health
```

### Metrics

```bash
# Prometheus metrics
curl https://yourdomain.com/actuator/prometheus

# JVM metrics
curl https://yourdomain.com/actuator/metrics/jvm.memory.used
```

### Logging

Configure centralized logging:

```yaml
# application-prod.yml
logging:
  file:
    name: /var/log/meal-service/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  level:
    root: INFO
    com.mealsubscription: INFO
```

**Log rotation** (logrotate):

```
/var/log/meal-service/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    create 0640 appuser appgroup
    sharedscripts
    postrotate
        systemctl reload meal-service
    endscript
}
```

## Backup & Recovery

### Database Backups

```bash
# Manual backup
pg_dump -h localhost -U meal_user mealdb_prod > backup_$(date +%Y%m%d).sql

# Automated backup script
#!/bin/bash
BACKUP_DIR="/backups/postgres"
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -h localhost -U meal_user mealdb_prod | gzip > $BACKUP_DIR/mealdb_$DATE.sql.gz
# Retention: keep last 30 days
find $BACKUP_DIR -name "mealdb_*.sql.gz" -mtime +30 -delete
```

### Restore Database

```bash
# Restore from backup
gunzip -c backup_20260302.sql.gz | psql -h localhost -U meal_user mealdb_prod
```

### Docker Volume Backups

```bash
# Backup volume
docker run --rm -v pgdata_prod:/data -v $(pwd):/backup \
  alpine tar czf /backup/pgdata_backup_$(date +%Y%m%d).tar.gz -C /data .

# Restore volume
docker run --rm -v pgdata_prod:/data -v $(pwd):/backup \
  alpine tar xzf /backup/pgdata_backup_20260302.tar.gz -C /data
```

## Troubleshooting

### Application Won't Start

```bash
# Check logs
docker-compose logs -f app

# Common issues:
# 1. Database connection failed → verify SPRING_DATASOURCE_URL
# 2. JWT secret missing → set JWT_SECRET environment variable
# 3. Port already in use → change port or stop conflicting service
```

### High Memory Usage

```bash
# Check JVM memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Adjust heap size
export JAVA_OPTS="-Xms512m -Xmx2g"
```

### Slow Database Queries

```sql
-- Enable query logging in PostgreSQL
ALTER SYSTEM SET log_min_duration_statement = 1000; -- Log queries > 1s
SELECT pg_reload_conf();

-- View slow queries
SELECT * FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;
```

### SSL Certificate Issues

```bash
# Verify certificate
openssl s_client -connect yourdomain.com:443 -servername yourdomain.com

# Check expiration
echo | openssl s_client -connect yourdomain.com:443 2>/dev/null | openssl x509 -noout -dates
```

## Rollback Strategy

### Quick Rollback

```bash
# Docker Compose
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml pull app  # Pull previous version
docker-compose -f docker-compose.prod.yml up -d

# ECS
aws ecs update-service --cluster prod-cluster \
  --service meal-subscription-service \
  --task-definition meal-subscription-service:PREVIOUS_VERSION
```

### Database Rollback

```bash
# Flyway undo (requires Flyway Teams edition)
mvn flyway:undo

# Manual rollback
psql -h localhost -U meal_user mealdb_prod < rollback_script.sql
```

## Performance Optimization

- **Database Indexing**: Ensure all foreign keys and frequently queried columns are indexed
- **Connection Pooling**: Tune HikariCP settings for your load
- **Caching**: Add Redis for session/data caching
- **CDN**: Serve static assets via CDN
- **Load Balancing**: Use multiple app instances behind load balancer
- **Database Read Replicas**: For read-heavy workloads

---

**Deployment Checklist Complete ✓**

For questions, see [CONTRIBUTING.md](CONTRIBUTING.md) or open an issue.
