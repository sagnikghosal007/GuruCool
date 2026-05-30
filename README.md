# GuruCool Backend 🎓

> **Production-grade distributed microservices backend for a college alumni mentorship platform.**  
> Alumni (mentors) connect with students (mentees) via verified profiles, 1:1/group sessions, paid webinars, and AI-powered mentor matching.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.6-black.svg)](https://kafka.apache.org/)

---

## 📐 Architecture Overview

```
                        ┌─────────────────────────────────────┐
                        │         API Gateway (:8080)          │
                        │  JWT Auth · Rate Limiting · Routing  │
                        └──────────────┬──────────────────────┘
                                       │ lb:// (Eureka)
         ┌─────────────┬───────────────┼───────────────┬──────────────┐
         ▼             ▼               ▼               ▼              ▼
   User Service   Mentor Service  Session Service  Payment Service  AI Service
     (:8081)        (:8082)          (:8083)         (:8084)        (:8086)
         │             │               │               │
         └─────────────┴───────────────┴───────────────┘
                                   │ Kafka Events
                                   ▼
                         Notification Service (:8085)
                         (email, push — Kafka consumer)

Supporting Infrastructure:
  Config Server (:8888) · Eureka Registry (:8761)
  PostgreSQL x4 · Redis · Kafka · Zipkin · MinIO
```

### Service Map

| Service | Port | Database | Responsibility |
|---|---|---|---|
| `api-gateway` | 8080 | — | JWT auth, rate limiting, Swagger aggregation, routing |
| `service-registry` | 8761 | — | Eureka service discovery |
| `config-server` | 8888 | — | Centralised config for all services |
| `user-service` | 8081 | `users_db` | Registration, login, JWT, profile, email verification |
| `mentor-service` | 8082 | `mentors_db` | Profiles, availability, ratings, verification |
| `session-service` | 8083 | `sessions_db` | Session booking, waitlist, recordings |
| `payment-service` | 8084 | `payments_db` | Mock payment engine, ledger, idempotency, refunds |
| `notification-service` | 8085 | — | Kafka-driven email notifications |
| `ai-service` | 8086 | Redis only | Mentor matching, career paths, skill gap analysis |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Virtual Threads for I/O-bound tasks) |
| Framework | Spring Boot 3.3 |
| Gateway | Spring Cloud Gateway |
| Service Discovery | Netflix Eureka |
| Config | Spring Cloud Config Server |
| Messaging | Apache Kafka (Spring Kafka, manual ack, DLT) |
| AI | Spring AI (OpenAI gpt-4o-mini; falls back to Mock Engine) |
| Database | PostgreSQL 16 (one schema per service) |
| Cache / Locks | Redis (Spring Data Redis, Lettuce pool) |
| Auth | JWT (JJWT 0.12), Spring Security (stateless) |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI per service + aggregated at gateway) |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| Logging | SLF4J + Logback, MDC trace correlation |
| Tracing | Micrometer Tracing + Zipkin |
| File Storage | MinIO (S3-compatible) |
| Payments | **Mock Payment Engine** (no 3rd party — swap-in ready for real gateway) |
| Build | Maven multi-module |
| Containerisation | Docker + docker-compose |

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21
- Maven 3.9+

### 1. Clone & Start Infrastructure

```bash
git clone https://github.com/your-org/gurucool-backend.git
cd gurucool-backend

# Start all infrastructure (PostgreSQL x4, Redis, Kafka, MinIO, Zipkin)
docker-compose up -d

# Wait ~30 seconds for services to be healthy
docker-compose ps
```

### 2. Build All Services

```bash
mvn clean install -DskipTests
```

### 3. Start Services (in order)

```bash
# Terminal 1 — Config Server
cd config-server && mvn spring-boot:run

# Terminal 2 — Service Registry (wait for config server)
cd service-registry && mvn spring-boot:run

# Terminal 3 — User Service
cd user-service && mvn spring-boot:run

# Terminal 4 — API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 5 — Mentor Service
cd mentor-service && mvn spring-boot:run

# Terminal 6 — Session Service
cd session-service && mvn spring-boot:run

# Terminal 7 — Payment Service
cd payment-service && mvn spring-boot:run

# Terminal 8 — Notification Service
cd notification-service && mvn spring-boot:run

# Terminal 9 — AI Service
cd ai-service && mvn spring-boot:run
```

### 4. Verify Everything Works

```bash
# Eureka Dashboard
open http://localhost:8761

# Aggregated Swagger UI (all services)
open http://localhost:8080/swagger-ui.html

# Zipkin distributed tracing
open http://localhost:9411

# Kafka UI
open http://localhost:8090

# MinIO Console
open http://localhost:9001   # user: gurucool_minio / pass: gurucool_minio_secret
```

---

## 🔐 Authentication Flow

```
POST /api/users/auth/register   → Create account (STUDENT or MENTOR)
POST /api/users/auth/login      → Get accessToken (15 min) + refreshToken (7 days)
POST /api/users/auth/refresh    → Rotate refresh token
POST /api/users/auth/logout     → Revoke refresh + blacklist access token
```

All subsequent requests require:
```
Authorization: Bearer <accessToken>
```

The gateway validates JWT, extracts `X-User-Id` and `X-User-Role`, and forwards them to downstream services.

**Roles:** `STUDENT` · `MENTOR` · `COLLEGE_ADMIN` · `PLATFORM_ADMIN`

---

## 💳 Mock Payment Flow

GuruCool uses a **self-contained mock payment engine** — no external payment gateway required.

### Option A — Two-step flow (mirrors real payment UX)

```bash
# Step 1: Create order
POST /api/payments/orders
Headers: X-Idempotency-Key: <uuid>, Authorization: Bearer <token>
Body: { "bookingId": "<uuid>", "amount": 499.00, "currency": "INR", "mentorId": "<uuid>" }

# Returns: { mockOrderId, mockSignature, ... }

# Step 2: Verify payment (use values from Step 1)
POST /api/payments/verify
Body: { "mockOrderId": "...", "mockPaymentId": "mock_pay_...", "mockSignature": "...", "bookingId": "..." }
```

### Option B — One-shot simulation (best for testing)

```bash
POST /api/payments/test/simulate
Body: { "bookingId": "<uuid>", "amount": 499.00, "mentorId": "<uuid>", "forceFailure": false }

# Set forceFailure: true to test the failure + refund path
```

**Success rate:** Configurable via `MOCK_PAYMENT_SUCCESS_RATE` env var (default: 95%).

---

## 🤖 AI Features

The AI service works **with or without an OpenAI API key**:

| Mode | Behaviour |
|---|---|
| `OPENAI_API_KEY` not set | Returns realistic mock responses instantly |
| `OPENAI_API_KEY` set | Calls `gpt-4o-mini` with 30s timeout + circuit breaker |

### Endpoints

```bash
POST /api/ai/match          # Rank top mentor matches for a student
POST /api/ai/career-path    # Generate personalized career roadmap
POST /api/ai/skill-gap      # Identify skills to learn for target role
POST /api/ai/session-summary # Generate session summary (MENTOR only)
GET  /api/ai/match/cache/{studentId}  # Return cached match result
```

---

## 📋 Environment Variables

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | `gurucool-super-secret-...` | HS256 JWT signing secret (min 256-bit) |
| `POSTGRES_USERS_HOST` | `localhost` | Users DB host |
| `POSTGRES_USERS_PORT` | `5432` | Users DB port |
| `POSTGRES_USERS_USER` | `gurucool` | Users DB username |
| `POSTGRES_USERS_PASSWORD` | `gurucool_secret` | Users DB password |
| `POSTGRES_MENTORS_HOST` | `localhost` | Mentors DB host |
| `POSTGRES_MENTORS_PORT` | `5433` | Mentors DB port |
| `POSTGRES_SESSIONS_HOST` | `localhost` | Sessions DB host |
| `POSTGRES_SESSIONS_PORT` | `5434` | Sessions DB port |
| `POSTGRES_PAYMENTS_HOST` | `localhost` | Payments DB host |
| `POSTGRES_PAYMENTS_PORT` | `5435` | Payments DB port |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | `gurucool_redis_secret` | Redis password |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `MINIO_ENDPOINT` | `http://localhost:9000` | MinIO S3 endpoint |
| `MINIO_ACCESS_KEY` | `gurucool_minio` | MinIO access key |
| `MINIO_SECRET_KEY` | `gurucool_minio_secret` | MinIO secret key |
| `EUREKA_SERVER_URL` | `http://localhost:8761/eureka/` | Eureka server |
| `ZIPKIN_URL` | `http://localhost:9411` | Zipkin tracing server |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP server |
| `MAIL_USERNAME` | `noreply@gurucool.com` | Email sender |
| `MAIL_PASSWORD` | — | Email password / app password |
| `OPENAI_API_KEY` | `demo-key` | OpenAI key (optional — mock used if absent) |
| `MOCK_PAYMENT_SECRET` | `gurucool-mock-payment-secret-key` | HMAC key for mock payment signing |
| `MOCK_PAYMENT_SUCCESS_RATE` | `95` | % of mock payments that succeed |

---

## 📡 API Endpoints Summary

### User Service (`/api/users`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register new user |
| POST | `/auth/login` | Public | Login → get JWT tokens |
| POST | `/auth/refresh` | Public | Rotate refresh token |
| POST | `/auth/logout` | Bearer | Logout + revoke tokens |
| GET | `/auth/verify-email` | Public | Verify email via token |
| GET | `/profile` | Bearer | Get own profile |
| PUT | `/profile` | Bearer | Update own profile |
| POST | `/profile/picture` | Bearer | Upload profile picture |
| GET | `/{userId}` | Admin | Get any user (admin) |
| PUT | `/{userId}/status` | Admin | Activate/deactivate user |

### Mentor Service (`/api/mentors`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/profile` | MENTOR | Create mentor profile |
| GET | `/profile` | Bearer | Get own mentor profile |
| PUT | `/profile` | MENTOR | Update mentor profile |
| GET | `/` | Public | Search/list mentors |
| GET | `/top` | Public | Top 10 mentors |
| GET | `/{id}` | Public | Get mentor by ID |
| POST | `/{id}/verify` | MENTOR | Submit verification request |
| PUT | `/{id}/verify/{reqId}` | ADMIN | Approve/reject verification |
| POST | `/{id}/availability` | MENTOR | Set weekly availability |
| GET | `/{id}/availability` | Public | Get availability slots |
| POST | `/{id}/rate` | STUDENT | Rate a mentor |
| GET | `/{id}/ratings` | Public | Get mentor ratings |

### Session Service (`/api/sessions`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/` | MENTOR | Create a session |
| GET | `/` | Public | List/filter sessions |
| GET | `/{id}` | Public | Get session details |
| PUT | `/{id}` | MENTOR | Update session |
| DELETE | `/{id}` | MENTOR/ADMIN | Cancel session |
| POST | `/{id}/book` | STUDENT | Book session (idempotent) |
| DELETE | `/{id}/book/{bookingId}` | STUDENT | Cancel booking |
| GET | `/my/booked` | STUDENT | My booked sessions |
| GET | `/my/hosting` | MENTOR | My hosted sessions |
| PUT | `/{id}/status` | MENTOR | Update session status |
| POST | `/{id}/recording` | MENTOR | Add recording URL |

### Payment Service (`/api/payments`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/orders` | Bearer | Create mock payment order |
| POST | `/verify` | Bearer | Verify mock payment signature |
| POST | `/test/simulate` | Bearer | 🧪 One-shot payment simulation |
| POST | `/refund` | ADMIN | Initiate refund |
| GET | `/history` | Bearer | Payment history |
| GET | `/{id}` | Bearer | Payment details |
| GET | `/mentor/earnings` | MENTOR | Earnings summary |
| GET | `/admin/dashboard` | ADMIN | Platform analytics |

### AI Service (`/api/ai`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/match` | Bearer | AI mentor matching |
| POST | `/career-path` | Bearer | Career roadmap generator |
| POST | `/skill-gap` | Bearer | Skill gap analysis |
| POST | `/session-summary` | MENTOR | Generate session summary |
| GET | `/match/cache/{studentId}` | Bearer | Get cached match result |

---

## 📨 Kafka Event Flow

```
user-service          → user.registered, user.email.verification
mentor-service        → mentor.verification.requested, mentor.verified
session-service       → session.booked, session.cancelled, session.completed,
                        payment.refund.requested, waitlist.promoted
payment-service       → payment.completed, payment.failed, payment.refunded

notification-service  ← ALL above topics (sends emails)
mentor-service        ← user.registered (auto-creates mentor shell)
session-service       ← payment.completed (confirms pending booking)
payment-service       ← session.cancelled, payment.refund.requested (auto-refund)
```

All topics use **3 partitions**, **manual offset acknowledgement**, and **Dead Letter Topics** (`{topic}.DLT`) after 3 retry attempts.

---

## 🏗️ Production Standards Applied

- **BigDecimal everywhere** for money — never `float` or `double`
- **Two-layer idempotency** — Redis (fast) + PostgreSQL (durable) for all payment operations
- **Pessimistic locking** (`SELECT FOR UPDATE`) on payment state transitions
- **Optimistic locking** (`@Version`) on all JPA entities
- **HikariCP** connection pool (max 20, min idle 5) per service
- **Redis caching** — mentor profiles (10 min), availability (2 min), top mentors (30 min), AI matches (30 min)
- **AOP logging** — entry/exit/execution time on all `@Service` methods
- **MDC trace correlation** — `traceId`, `userId`, `serviceId` on every request
- **Structured logging** — JSON-ready Logback config with MDC fields
- **Bean Validation** — `@Valid` on all request DTOs with custom error messages
- **Global exception handler** — `@RestControllerAdvice` with standard error envelope
- **Async processing** — `@Async` (core 10, max 50 threads) for non-critical paths
- **Virtual threads** — Kafka consumers run on virtual threads (Java 21)
- **Database indexes** — on all FK columns and frequently queried fields
- **Schema-first** — `schema.sql` per service (no `ddl-auto=create-drop` in production)

---

## 📁 Project Structure

```
gurucool-backend/
├── pom.xml                     # Parent POM (Spring Boot 3.3, Spring Cloud 2023.0)
├── docker-compose.yml          # Full infrastructure
├── README.md
├── common/                     # Shared: BaseEntity, DTOs, exceptions, Kafka events, AOP
├── config-server/              # Spring Cloud Config Server (port 8888)
├── service-registry/           # Eureka Server (port 8761)
├── api-gateway/                # Gateway: JWT filter, rate limit, Swagger aggregation (port 8080)
├── user-service/               # Auth + profiles (port 8081) → users_db
├── mentor-service/             # Mentor management (port 8082) → mentors_db
├── session-service/            # Session booking (port 8083) → sessions_db
├── payment-service/            # Mock payments + ledger (port 8084) → payments_db
├── notification-service/       # Kafka-driven emails (port 8085)
└── ai-service/                 # AI matching + career paths (port 8086) → Redis
```

---

## 🧪 Testing the Full Flow

```bash
# 1. Register a mentor
curl -X POST http://localhost:8080/api/users/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Priya Sharma","email":"priya@iitd.ac.in","password":"Secure@123","role":"MENTOR"}'

# 2. Register a student
curl -X POST http://localhost:8080/api/users/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Arjun Mehta","email":"arjun@student.iitd.ac.in","password":"Secure@123","role":"STUDENT"}'

# 3. Login as mentor → get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"priya@iitd.ac.in","password":"Secure@123"}' | jq -r '.data.accessToken')

# 4. Create a session
curl -X POST http://localhost:8080/api/sessions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"System Design Deep Dive","sessionType":"ONE_ON_ONE","scheduledAt":"2025-07-01T10:00:00Z","durationMinutes":60,"maxParticipants":1,"isPaid":true,"priceAmount":499.00}'

# 5. Login as student and book the session
STUDENT_TOKEN=$(curl -s -X POST http://localhost:8080/api/users/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"arjun@student.iitd.ac.in","password":"Secure@123"}' | jq -r '.data.accessToken')

curl -X POST http://localhost:8080/api/sessions/<SESSION_ID>/book \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "X-Idempotency-Key: $(uuidgen)"

# 6. Simulate payment
curl -X POST http://localhost:8080/api/payments/test/simulate \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bookingId":"<BOOKING_ID>","amount":499.00,"forceFailure":false}'

# 7. Get AI mentor match
curl -X POST http://localhost:8080/api/ai/match \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"studentId":"<STUDENT_ID>","careerGoal":"Become a Staff Engineer","skills":["Java","Spring"],"preferredDomain":"Backend"}'
```

---

## 🌐 Infrastructure Ports

| Service | Port | URL |
|---|---|---|
| API Gateway | 8080 | http://localhost:8080/swagger-ui.html |
| Eureka Dashboard | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888 |
| User Service | 8081 | http://localhost:8081/swagger-ui.html |
| Mentor Service | 8082 | http://localhost:8082/swagger-ui.html |
| Session Service | 8083 | http://localhost:8083/swagger-ui.html |
| Payment Service | 8084 | http://localhost:8084/swagger-ui.html |
| Notification Service | 8085 | http://localhost:8085/api/notifications/health |
| AI Service | 8086 | http://localhost:8086/swagger-ui.html |
| PostgreSQL (users) | 5432 | users_db |
| PostgreSQL (mentors) | 5433 | mentors_db |
| PostgreSQL (sessions) | 5434 | sessions_db |
| PostgreSQL (payments) | 5435 | payments_db |
| Redis | 6379 | — |
| Kafka | 9092 | — |
| Kafka UI | 8090 | http://localhost:8090 |
| Zipkin | 9411 | http://localhost:9411 |
| MinIO API | 9000 | — |
| MinIO Console | 9001 | http://localhost:9001 |

---

## 🔮 Upgrading to Real Payments

The mock payment engine is designed as a **drop-in replacement**. To switch to a real gateway:

1. Add the payment SDK dependency to `payment-service/pom.xml`
2. Create a `RealPaymentEngine` that implements the same 3 methods as `MockPaymentEngine` (`createOrder`, `capturePayment`, `refund`)
3. Inject `RealPaymentEngine` instead of `MockPaymentEngine` in `PaymentService`
4. Update the config with real API keys

No other service code needs to change.

---

## 👨‍💻 Built With

- **Java 21** — virtual threads, records, pattern matching
- **Spring Boot 3.3** — production-ready microservices
- **Spring Cloud 2023.0** — service mesh (Gateway, Eureka, Config)
- **Apache Kafka** — event-driven architecture
- **PostgreSQL + Redis** — persistence + caching
- **Spring AI** — LLM integration with graceful fallback
- **MinIO** — S3-compatible file storage
- **Docker Compose** — one-command infrastructure

---

*GuruCool — connecting the next generation of engineers with the mentors who built the industry.*
