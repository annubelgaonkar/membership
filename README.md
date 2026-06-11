# FirstClub Membership

A backend system for a subscription-based membership program with tiered benefits, built with Spring Boot. Designed for extensibility, clean abstractions, and concurrent safety.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 (in-memory, zero setup) |
| Build Tool | Maven |
| Utilities | Lombok, Jakarta Validation |

---

## Architecture Overview

```
fc-membership/
├── controller/         # REST API layer
├── service/
│   └── impl/           # Business logic
├── repository/         # Spring Data JPA repositories
├── entity/             # JPA entities (DB models)
├── dto/
│   ├── request/        # Inbound API payloads
│   └── response/       # Outbound API responses
├── enums/              # PlanDuration, TierType, SubscriptionStatus, CriteriaType
├── exception/          # Custom exceptions + GlobalExceptionHandler
├── tier/               # Tier evaluation engine (Strategy pattern)
│   └── criteria/       # Pluggable criteria evaluators
├── mapper/             # Entity → DTO conversion
└── config/             # DataSeeder (auto-populates DB on startup)
```

---

## Domain Design

### Entities

| Entity | Description |
|---|---|
| `MembershipPlan` | Monthly / Quarterly / Yearly plans with pricing |
| `MembershipTier` | Silver / Gold / Platinum tiers per plan |
| `TierCriteria` | Configurable rules that qualify a user for a tier |
| `User` | User with order history and cohort |
| `UserSubscription` | Active subscription linking user → plan → tier |

### Relationships
```
MembershipPlan
    └── MembershipTier (Silver, Gold, Platinum)
            └── TierCriteria (MIN_ORDER_COUNT / MIN_ORDER_VALUE / USER_COHORT)

User
    └── UserSubscription → MembershipPlan + MembershipTier
```

---

## Key Design Decisions

### 1. Tier Criteria — Strategy Pattern
Each criteria type is a separate `@Component` implementing `TierCriteriaEvaluator`. The `TierEvaluationEngine` auto-discovers all evaluators via Spring injection and maps them by `CriteriaType`.

```
TierCriteriaEvaluator (interface)
    ├── MinOrderCountEvaluator   → CriteriaType.MIN_ORDER_COUNT
    ├── MinOrderValueEvaluator   → CriteriaType.MIN_ORDER_VALUE
    └── UserCohortEvaluator      → CriteriaType.USER_COHORT
```

Adding a new criteria type requires **zero changes** to existing code — just add a new evaluator class.

### 2. Auto Tier Assignment on Subscribe
When a user subscribes to a plan, the system automatically assigns the best tier they qualify for based on their order history and cohort — users cannot manually pick a tier. If a user doesn't qualify for any tier, they are defaulted to Silver so anyone can subscribe.

```
User subscribes to Monthly Plan
    → TierEvaluationEngine evaluates criteria (highest tier first)
    → Assigns best qualifying tier
    → Defaults to Silver if no criteria met
```

### 3. Concurrency — Optimistic Locking
`UserSubscription` uses `@Version` (JPA optimistic locking). If two concurrent requests try to modify the same subscription (e.g. simultaneous upgrade + cancel), one will succeed and the other will receive a `409 Conflict` response — no silent data corruption.

### 4. Configurable Tiers
Tier criteria are stored as DB rows, not hardcoded logic. Thresholds (order count, order value, cohort name) can be updated in the DB without any code change.

### 5. Uniform API Response
All endpoints return a consistent `ApiResponse<T>` wrapper:
```json
// Success
{ "success": true, "message": "...", "data": { ... }, "timestamp": "..." }

// Error
{ "success": false, "message": "...", "timestamp": "..." }
```

---

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.8+

### Start
```bash
mvn spring-boot:run
```

App starts on **http://localhost:8080**

### H2 Console (inspect DB)
```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:firstclubdb
Username: sa
Password: (leave blank)
```

### Seed Data
On startup, the `DataSeeder` automatically populates:
- **3 Plans** — Monthly (₹99), Quarterly (₹249), Yearly (₹799)
- **9 Tiers** — Silver / Gold / Platinum per plan with criteria
- **3 Users** — Rahul (high activity), Priya (premium cohort), Amit (low activity)

---

## API Reference

Base URL: `http://localhost:8080/api/v1/membership`

### GET `/plans`
Returns all active membership plans with their tiers and criteria.

**Response**
```json
{
  "success": true,
  "message": "Available membership plans",
  "data": [ { "id": 1, "name": "Monthly Membership", "price": 99.00, "tiers": [...] } ]
}
```

---

### POST `/subscribe`
Subscribe a user to a plan. The system automatically assigns the best tier the user qualifies for based on their order history and cohort. Defaults to Silver if no criteria are met.

**Request**
```json
{ "userId": 1, "planId": 1 }
```

**Validations**
- User must exist
- Plan must exist and be active
- User must not have an existing active subscription

**Tier Assignment Logic**
```
Rahul  (orderValue=₹3500, Monthly) → Platinum (meets ₹3000 threshold)
Priya  (cohort=PREMIUM_INVITE, Quarterly) → Platinum (cohort match)
Amit   (orderValue=₹200, Monthly) → Silver (default, no criteria met)
```

---

### PUT `/tier`
Upgrade or downgrade tier within the same plan.

**Request**
```json
{ "userId": 1, "newTierId": 2 }
```

**Validations**
- New tier must belong to the user's current plan
- Cannot change to the same tier

---

### PUT `/cancel/{userId}`
Cancel an active subscription.

---

### GET `/status/{userId}`
Get current active subscription with days remaining.

---

### PUT `/evaluate-tier/{userId}`
Re-evaluates and auto-assigns the best tier the user currently qualifies for. Use this when a user's order activity changes after subscription.

---

## Demo Flow

```
1. GET  /plans                       → browse available plans
2. POST /subscribe { userId:1, planId:1 }  → Rahul auto-assigned Platinum
3. POST /subscribe { userId:2, planId:2 }  → Priya auto-assigned Platinum (cohort)
4. POST /subscribe { userId:3, planId:1 }  → Amit defaulted to Silver
5. POST /subscribe (Rahul again)     → 400 — already subscribed
6. GET  /status/1                    → active, 30 days remaining
7. PUT  /tier { newTierId: 2 }       → manually downgrade to Gold
8. PUT  /tier { newTierId: 4 }       → 400 — tier from different plan
9. PUT  /evaluate-tier/1             → engine re-assigns Platinum
10. PUT /cancel/1                    → subscription cancelled
11. GET /status/1                    → 404 — no active subscription
```

---

## Error Handling

| Exception | HTTP Status | When |
|---|---|---|
| `ResourceNotFoundException` | 404 | User / Plan / Tier not found |
| `SubscriptionException` | 400 | Double subscribe, no active sub |
| `TierMismatchException` | 400 | Tier doesn't belong to plan |
| `MethodArgumentNotValidException` | 400 | Request validation failure |
| `ObjectOptimisticLockingFailureException` | 409 | Concurrent subscription modification |

---

## Extensibility

| What to extend | How |
|---|---|
| Add a new criteria type | Create a new `TierCriteriaEvaluator` implementation — engine picks it up automatically |
| Add a new plan duration | Add value to `PlanDuration` enum and seed a new plan |
| Add a new tier | Insert a new `MembershipTier` row with criteria — no code change |
| Add new API versions | New controller under `/api/v2/` — existing v1 untouched |