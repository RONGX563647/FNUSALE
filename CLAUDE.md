# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

校园二手交易平台 (Campus Second-hand Trading Platform) - A lightweight, trust-based second-hand trading platform for university students and staff.

**Tech Stack**: Java 21 + Spring Boot 3.2+ + Spring Cloud Alibaba 2023.x

**Core Features**:
- Campus identity verification (student ID/employee ID authentication)
- Location-based verification (campus geofencing)
- AI-powered product categorization
- Instant messaging (IM) for buyer-seller communication
- Seckill (flash sale) and coupon system
- Campus pickup point management

## Common Commands

### Docker Infrastructure

```bash
# Start all infrastructure services
cd backend/docker
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f [service_name]

# Stop all services
docker-compose down

# Stop and remove volumes (clear all data)
docker-compose down -v
```

### Maven Build (when Java code exists)

```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl services/user-service -am

# Run a single service
mvn spring-boot:run -pl services/user-service

# Run tests
mvn test
mvn test -Dtest=ClassName#methodName
```

### Run Single Test

```bash
mvn test -pl services/user-service -Dtest=UserServiceTest#testUserRegistration
```

## Architecture

### Microservices

| Service | Responsibility |
|---------|---------------|
| `gateway-service` | API gateway, routing, authentication, rate limiting |
| `user-service` | User registration, campus verification, permissions |
| `product-service` | Product listing, AI categorization, search, status management |
| `im-service` | Instant messaging, message notifications, chat history |
| `ai-guide-service` | Personalized recommendations, price suggestions, smart Q&A |
| `marketing-service` | Coupons, seckill activities, promotional campaigns |
| `trade-service` | Orders, payment integration, pickup confirmation, disputes |
| `admin-service` | Product audit, marketing config, statistics dashboard |
| `common-service` | Shared utilities, logging, third-party integrations |

### Layered Architecture

```
Frontend (WeChat Mini Program / H5 Admin)
    ↓
Gateway Layer (Spring Cloud Gateway)
    ↓
Application Layer (Microservices)
    ↓
Domain Layer (Business Logic)
    ↓
Infrastructure Layer (MySQL, Redis, RocketMQ, MinIO, Elasticsearch)
```

## Infrastructure Services

| Service | Port | Console URL | Credentials |
|---------|------|-------------|-------------|
| MySQL | 3306 | - | fnusale/fnusale123456 |
| Redis | 6379 | - | password: redis123456 |
| Nacos | 8848 | http://localhost:8848/nacos | nacos/nacos |
| Sentinel | 8858 | http://localhost:8858 | sentinel/sentinel123456 |
| MinIO | 9000/9001 | http://localhost:9001 | admin/minio123456 |
| RocketMQ Dashboard | 8180 | http://localhost:8180 | - |
| Elasticsearch | 9200 | http://localhost:9200 | - |
| Prometheus | 9090 | http://localhost:9090 | - |
| Grafana | 3000 | http://localhost:3000 | admin/grafana123456 |

## Configuration Management

- **Nacos**: Centralized configuration for all microservices
- **Environment Variables**: Copy `backend/docker/.env.example` to `.env` and customize
- **Sensitive Data**: Never commit `.env` files (already in `.gitignore`)

## Database Schema

Complete database design is documented in [doc/数据库设计.md](../doc/数据库设计.md).

Key modules:
- User: `t_user`, `t_campus_pick_point`, `t_user_address`
- Product: `t_product`, `t_product_category`, `t_product_image`
- IM: `t_im_session`, `t_im_message`, `t_im_quick_reply`
- Marketing: `t_coupon`, `t_user_coupon`, `t_seckill_activity`
- Trade: `t_order`, `t_order_evaluation`, `t_trade_dispute`

## Key Design Decisions

### Campus Verification Flow
1. User uploads student ID/employee card image
2. Admin reviews and approves/rejects
3. Only verified users can publish products or make purchases
4. Student ID is stored with masking (last 4 digits visible)

### Location Verification
- Uses Amap (高德地图) API for geocoding and geofencing
- Campus fence coordinates configured in `t_system_config`
- Users must be within campus boundaries to publish/trade

### Seckill (Flash Sale) Implementation
- Redis pre-loads inventory for stock deduction
- Sentinel rate limiting (QPS threshold: 500 for campus scale)
- RocketMQ async order processing
- No distributed lock needed (campus user scale ≤100K)

### AI Integration
- Product categorization: Alibaba Cloud Vision AI
- Price suggestions: Based on historical transaction data
- Recommendations: Lightweight collaborative filtering

## Project Structure

```
backend/
├── docker/           # Docker Compose configuration
│   ├── docker-compose.yml
│   ├── .env.example
│   ├── mysql/init/   # MySQL initialization scripts
│   └── prometheus/   # Prometheus configuration
├── services/         # Microservice modules (to be implemented)
├── common/           # Shared module (to be implemented)
└── pom.xml           # Maven parent POM (to be created)
```

## Documentation

- Architecture & Features: [doc/功能分析与架构选型.md](../doc/功能分析与架构选型.md)
- Database Design: [doc/数据库设计.md](../doc/数据库设计.md)
- Backend README: [backend/README.md](README.md)