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

### Backend (Maven)

```bash
cd backend

# Build all modules
mvn clean install

# Build specific module (skip tests)
mvn clean install -pl fnusale-user -am

# Run a single service
mvn spring-boot:run -pl fnusale-user

# Run tests
mvn test
mvn test -Dtest=ClassName#methodName

# Run single test in specific module
mvn test -pl fnusale-user -Dtest=UserServiceTest#testUserRegistration
```

### Frontend (Vue3 + Vite)

```bash
cd frontend

# Install dependencies
npm install

# Start development server (port 3000)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Architecture

### Microservices

| Service | Port | Responsibility |
|---------|------|---------------|
| `fnusale-gateway` | 8080 | API gateway, routing, authentication, rate limiting |
| `fnusale-user` | 8101 | User registration, campus verification, permissions |
| `fnusale-product` | 8102 | Product listing, AI categorization, search, status management |
| `fnusale-im` | 8103 | Instant messaging, message notifications, chat history |
| `fnusale-ai-guide` | 8104 | Personalized recommendations, price suggestions, smart Q&A |
| `fnusale-marketing` | 8105 | Coupons, seckill activities, promotional campaigns |
| `fnusale-trade` | 8106 | Orders, payment integration, pickup confirmation, disputes |
| `fnusale-admin` | 8107 | Product audit, marketing config, statistics dashboard |
| `fnusale-common` | - | Shared utilities, logging, third-party integrations |

### API Routing (Gateway)

All API requests go through the gateway at port 8080 with path prefix `/api`:

| Path Pattern | Target Service |
|--------------|---------------|
| `/api/user/**` | fnusale-user |
| `/api/product/**` | fnusale-product |
| `/api/im/**` | fnusale-im |
| `/api/ai-guide/**` | fnusale-ai-guide |
| `/api/marketing/**` | fnusale-marketing |
| `/api/trade/**` | fnusale-trade |
| `/api/admin/**` | fnusale-admin |

### Frontend Architecture

```
frontend/src/
├── api/          # API modules (axios wrappers per domain)
├── components/   # Reusable Vue components
├── router/       # Vue Router configuration with auth guards
├── stores/       # Pinia stores (user, app state)
├── types/        # TypeScript interfaces (user, product, trade, etc.)
├── utils/        # Utilities (request.ts with axios interceptors)
└── views/        # Page components (Home, Login, Register, etc.)
```

Key frontend patterns:
- **State Management**: Pinia stores in `stores/` with composition API
- **API Layer**: `utils/request.ts` provides `http.get/post/put/delete` with token handling
- **Auth**: JWT tokens stored in localStorage, auto-refresh on 401
- **Routes**: Auth-guarded routes use `meta.requiresAuth: true`

### Layered Architecture

```
Frontend (Vue3 + Vite)
    ↓
Gateway Layer (Spring Cloud Gateway)
    ↓
Application Layer (Microservices)
    ↓
Domain Layer (Business Logic)
    ↓
Infrastructure Layer (MySQL, Redis, RabbitMQ, MinIO, Elasticsearch)
```

## Infrastructure Services

| Service | Port | Console URL | Credentials |
|---------|------|-------------|-------------|
| MySQL | 3307 | - | fnusale/fnusale123456 |
| Redis | 6379 | - | password: redis123456 |
| Nacos | 8848 | http://localhost:8848/nacos | nacos/nacos |
| Sentinel | 8858 | http://localhost:8858 | sentinel/sentinel123456 |
| MinIO | 9000/9001 | http://localhost:9001 | admin/minio123456 |
| RabbitMQ | 5672/15672 | http://localhost:15672 | admin/rabbitmq123456 |
| Elasticsearch | 9200 | http://localhost:9200 | - |
| Prometheus | 9090 | http://localhost:9090 | - |
| Grafana | 3000 | http://localhost:3000 | admin/grafana123456 |

## Configuration Management

- **Nacos**: Centralized configuration for all microservices
- **Environment Variables**: Copy `backend/docker/.env.example` to `.env` and customize
- **Sensitive Data**: Never commit `.env` files (already in `.gitignore`)
- **Service Config**: Each service uses `application.yml` with environment variable defaults

## Database Schema

Complete database design is documented in [doc/数据库设计.md](doc/数据库设计.md).

Key tables:
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
- Redis pre-loads inventory for stock deductions
- Sentinel rate limiting (QPS threshold: 500 for campus scale)
- RabbitMQ async order processing
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
├── fnusale-common/   # Shared module (entities, DTOs, utils)
├── fnusale-gateway/  # API Gateway
├── fnusale-user/     # User service
├── fnusale-product/  # Product service
├── fnusale-im/       # IM service
├── fnusale-ai-guide/ # AI guide service
├── fnusale-marketing/# Marketing service
├── fnusale-trade/    # Trade service
├── fnusale-admin/    # Admin service
└── pom.xml           # Maven parent POM

frontend/
├── src/
│   ├── api/          # API modules
│   ├── components/   # Vue components
│   ├── router/       # Vue Router
│   ├── stores/       # Pinia stores
│   ├── types/        # TypeScript types
│   ├── utils/        # Utilities
│   └── views/        # Page components
├── package.json
└── vite.config.ts
```

## Documentation

- Architecture & Features: [doc/功能分析与架构选型.md](doc/功能分析与架构选型.md)
- Database Design: [doc/数据库设计.md](doc/数据库设计.md)
- API Documentation: [doc/API接口文档.md](doc/API接口文档.md)
- Backend README: [backend/README.md](backend/README.md)