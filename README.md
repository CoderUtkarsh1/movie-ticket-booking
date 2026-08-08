# Movie Ticket Booking — Microservices Platform

A production-standard microservices application for movie ticket booking built with Spring Boot 3.5.14, Java 21, React, and Kubernetes.

## Architecture

```
Frontend (React) → API Gateway → Eureka Discovery
                                    ↓
              ┌─────────────────────────────────────────┐
              │  user  │ movie │ theater │ show │ booking│ payment │ notification
              └─────────────────────────────────────────┘
                  ↓         ↓        ↓       ↓       ↓        ↓          ↓
              SQL Server  MongoDB  SQL    SQL+Redis  SQL+Kafka  SQL     MongoDB+Kafka
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.5.14, Java 21 |
| Frontend | React 19, Vite 8, Redux Toolkit |
| Databases | SQL Server 2022, MongoDB 7 |
| Cache | Redis 7 |
| Messaging | Apache Kafka (Confluent 7.6) |
| Discovery | Eureka |
| Gateway | Spring Cloud Gateway |
| CI/CD | GitHub Actions |
| Container | Docker (multi-stage builds) |
| Orchestration | Kubernetes (Docker Desktop) |

## Quick Start

### Prerequisites
- Java 21, Node.js 20, Docker Desktop

### Local Development
```bash
# 1. Copy environment file
cp .env.example .env
# 2. Fill in your secrets in .env

# 3. Start infrastructure
docker compose up -d

# 4. Run services (each in separate terminal)
cd config-server && ./mvnw spring-boot:run
cd eureka-server && ./mvnw spring-boot:run
# ... start remaining services

# 5. Run frontend
cd frontend && npm install && npm run dev
```

### Run Tests
```bash
# Backend (per service)
cd booking-service && ./mvnw verify

# Frontend
cd frontend && npm test
```

### Deploy to Kubernetes
```bash
# Enable K8s in Docker Desktop (kind, 1 node, v1.34.3)
.\k8s\deploy.ps1

# Access
# Frontend: http://localhost:30000
# API:      http://localhost:30080
```

## Project Structure
```
movie-ticket-booking/
├── .github/workflows/ci.yml    ← CI/CD pipeline
├── .env.example                ← Environment template
├── docker-compose.yaml         ← Local infrastructure
├── k8s/                        ← Kubernetes manifests
│   ├── namespace.yaml
│   ├── secrets.yaml
│   ├── configmap.yaml
│   ├── infra/                  ← SQL, Mongo, Redis, Kafka
│   ├── services/               ← All microservice deployments
│   ├── deploy.ps1              ← Deploy script
│   └── teardown.ps1            ← Cleanup script
├── common-lib/                 ← Shared DTOs, exceptions
├── config-server/              ← Centralized config (port 8888)
├── eureka-server/              ← Service discovery (port 8761)
├── api-gateway/                ← Entry point (port 8080)
├── user-service/               ← Auth + JWT (port 8081)
├── movie-service/              ← Movie catalog (port 8082)
├── theater-service/            ← Theater management (port 8083)
├── show-service/               ← Shows + seat locking (port 8084)
├── booking-service/            ← Booking flow (port 8085)
├── payment-service/            ← Wallet + payments (port 8086)
├── notification-service/       ← Kafka consumer (port 8087)
├── frontend/                   ← React SPA
└── k6/                         ← Performance tests
```

## Testing (147 tests)

| Type | Count | Tool |
|------|-------|------|
| Unit Tests | 48 | JUnit 5 + Mockito |
| Controller Tests | 18 | @WebMvcTest |
| Repository Tests | 18 | Testcontainers (SQL Server) |
| Architecture Tests | 15 | ArchUnit |
| WireMock Tests | 8 | WireMock 3.9 |
| Integration Tests | 7 | @SpringBootTest + Testcontainers |
| API Tests | 7 | REST Assured |
| Contract Tests | 6 | Consumer-driven |
| Frontend Tests | 19 | Vitest + RTL |
| Performance | 1 script | k6 |
| **Total** | **~147** | |

## CI/CD Pipeline

```
Push → Build common-lib → Test 10 services (parallel)
  → JaCoCo coverage → Trivy security scan
  → Docker build → Push to DockerHub → Deploy to K8s
```

## License
MIT

