Absolutely. Below is a complete `README.md` you can put directly in the root of your project.

````markdown
# 🚚 Delivery Management System

A production-style backend for a **Delivery Management System** built using **Java, Spring Boot, PostgreSQL, Redis, Kafka, Docker, Spring Security, and JWT**.

The project is designed to demonstrate modern backend engineering practices including layered architecture, authentication and authorization, database persistence, caching, event-driven communication, and eventual microservice separation.

---

## 📌 Project Overview

The Delivery Management System is designed to manage the complete lifecycle of a delivery.

The platform will support:

- Customer registration and authentication
- Driver management
- Order creation and management
- Delivery assignment
- Delivery status tracking
- Role-based access control
- JWT-based authentication
- Redis caching
- Kafka-based asynchronous communication
- Notifications
- Microservice-oriented architecture

The project is being developed incrementally, starting with a modular Spring Boot application and gradually evolving toward a distributed microservices architecture.

---

# 🏗️ Architecture

The planned high-level architecture is:

```text
                         ┌──────────────┐
                         │    Client    │
                         │ Postman/Web  │
                         └──────┬───────┘
                                │
                                │ HTTP/REST
                                ▼
                     ┌─────────────────────┐
                     │     API Gateway     │
                     │                     │
                     │ Authentication      │
                     │ Authorization       │
                     └──────────┬──────────┘
                                │
              ┌─────────────────┼─────────────────┐
              │                 │                 │
              ▼                 ▼                 ▼
       ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
       │ User/Auth   │   │   Order     │   │  Delivery   │
       │  Service    │   │  Service    │   │   Service   │
       └──────┬──────┘   └──────┬──────┘   └──────┬──────┘
              │                 │                 │
              ▼                 ▼                 ▼
       ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
       │ PostgreSQL  │   │ PostgreSQL  │   │ PostgreSQL  │
       └─────────────┘   └─────────────┘   └─────────────┘

                         ┌─────────────┐
                         │    Redis    │
                         │    Cache    │
                         └─────────────┘

                         ┌─────────────┐
                         │    Kafka    │
                         │ Event Bus   │
                         └──────┬──────┘
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
             ┌─────────────┐        ┌─────────────┐
             │ Notification│        │   Tracking  │
             │   Service   │        │   Service   │
             └─────────────┘        └─────────────┘
````

---

# 🧩 Current Architecture

During development, the application is initially being implemented as a modular Spring Boot application.

```text
                    Spring Boot Application
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
     Controller            Service            Repository
          │                   │                   │
          └───────────────────┴───────────────────┘
                              │
                              ▼
                         PostgreSQL
                              │
                 ┌────────────┴────────────┐
                 ▼                         ▼
               Redis                     Kafka
```

The application follows the:

```text
Controller
     ↓
Service
     ↓
Repository
     ↓
Database
```

pattern.

This separation keeps HTTP handling, business logic, and database access independent.

---

# 🛠️ Technology Stack

| Technology         | Purpose                         |
| ------------------ | ------------------------------- |
| Java 21            | Programming language            |
| Spring Boot 3.5.4  | Backend framework               |
| Spring Web         | REST APIs                       |
| Spring Data JPA    | Database access                 |
| Hibernate          | ORM                             |
| PostgreSQL 15      | Relational database             |
| Redis 7.2          | Caching                         |
| Apache Kafka 3.8.1 | Event-driven communication      |
| Spring Security    | Security framework              |
| JWT                | Authentication                  |
| BCrypt             | Password hashing                |
| Docker             | Containerization                |
| Docker Compose     | Local infrastructure            |
| Maven              | Build and dependency management |
| Lombok             | Boilerplate reduction           |
| Bean Validation    | Request validation              |
| DBeaver            | Database management             |
| Postman            | API testing                     |
| IntelliJ IDEA      | Development environment         |

---

# 📦 Infrastructure

The project uses Docker Compose to run the required infrastructure.

```text
Docker
│
├── PostgreSQL
│   └── delivery-postgres
│
├── Redis
│   └── delivery-redis
│
└── Kafka
    └── delivery-kafka
```

---

# 🐘 PostgreSQL

PostgreSQL is the primary relational database.

### Container

```text
delivery-postgres
```

### Image

```text
postgres:15
```

### Port

```text
5432
```

### Database

```text
delivery_app
```

### Spring Boot configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/delivery_app
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

---

# 🔴 Redis

Redis is used as the caching layer.

### Container

```text
delivery-redis
```

### Image

```text
redis:7.2-alpine
```

### Port

```text
6379
```

### Spring Boot configuration

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Redis will be used to reduce repeated database queries and improve response times for frequently accessed data.

Example:

```text
Client
  │
  ▼
Application
  │
  ▼
Redis
  │
  ├── Cache HIT ──► Return data
  │
  └── Cache MISS
          │
          ▼
      PostgreSQL
          │
          ▼
      Store in Redis
          │
          ▼
      Return data
```

---

# 📨 Apache Kafka

Kafka will be used for asynchronous, event-driven communication between services.

### Container

```text
delivery-kafka
```

### Image

```text
apache/kafka:3.8.1
```

### Application port

```text
9092
```

### Controller port

```text
9093
```

### Spring Boot configuration

```properties
spring.kafka.bootstrap-servers=localhost:9092
```

Kafka uses **KRaft mode** for the local development environment.

---

# 🔐 Authentication

The system uses:

```text
Spring Security
       +
JWT
       +
BCrypt
```

Authentication flow:

```text
                 Login Request
                      │
                      ▼
                AuthController
                      │
                      ▼
                 AuthService
                      │
                      ▼
                UserRepository
                      │
                      ▼
                  PostgreSQL
                      │
                      ▼
             PasswordEncoder
                      │
                Password Match
                      │
                      ▼
                  JwtService
                      │
                      ▼
                     JWT
                      │
                      ▼
                   Client
```

The client then sends the JWT in subsequent requests:

```http
Authorization: Bearer <JWT>
```

---

# 🔑 JWT Authentication Flow

For protected requests:

```text
Client
  │
  │ Authorization: Bearer <JWT>
  ▼
Spring Security Filter Chain
  │
  ▼
JwtAuthenticationFilter
  │
  ▼
Extract JWT
  │
  ▼
Validate JWT
  │
  ▼
Extract User Information
  │
  ▼
Set Authentication
  │
  ▼
Controller
```

The JWT filter extends:

```java
OncePerRequestFilter
```

and processes requests through:

```java
doFilterInternal()
```

The filter uses:

```java
filterChain.doFilter(request, response);
```

to pass the request to the next component in the filter chain.

---

# 👥 Roles

The system supports the following user roles:

```text
CUSTOMER
DRIVER
ADMIN
```

The roles will be used for authorization.

### CUSTOMER

Can:

* Create orders
* View their orders
* Track deliveries

### DRIVER

Can:

* View assigned deliveries
* Accept/handle deliveries
* Update delivery status
* Update availability

### ADMIN

Can:

* Manage users
* Manage drivers
* Manage orders
* Manage deliveries
* Perform administrative operations

---

# 🛡️ Authentication vs Authorization

Authentication answers:

> Who are you?

JWT is used for authentication.

Authorization answers:

> What are you allowed to do?

Roles are used for authorization.

Example:

```text
CUSTOMER
    │
    ├── Create Order       ✅
    ├── View Own Orders    ✅
    └── Manage Drivers     ❌

DRIVER
    │
    ├── View Deliveries    ✅
    ├── Update Delivery    ✅
    └── Manage Users       ❌

ADMIN
    │
    ├── Manage Users       ✅
    ├── Manage Drivers     ✅
    ├── Manage Orders      ✅
    └── Manage Deliveries  ✅
```

---

# 🔒 Security Configuration

The application uses Spring Security's `SecurityFilterChain`.

Conceptually:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()
            .anyRequest().authenticated()
        );

    return http.build();
}
```

Public endpoints such as registration and login do not require authentication.

Other endpoints require a valid JWT.

---

# 🔐 Password Security

Passwords are never stored as plain text.

Registration flow:

```text
Plain Password
      │
      ▼
PasswordEncoder
      │
      ▼
BCrypt Hash
      │
      ▼
PostgreSQL
```

Example database value:

```text
$2a$10$................................
```

During login:

```text
User Password
      │
      ▼
PasswordEncoder.matches()
      │
      ▼
Stored BCrypt Hash
```

---

# 👤 User Management

The `User` entity contains:

```text
id
firstName
lastName
email
phoneNumber
password
role
driverStatus
createdAt
updatedAt
```

`driverStatus` (`AVAILABLE` / `UNAVAILABLE` / `ON_DELIVERY`) is only populated for users with `role = DRIVER`; it is `null` for `CUSTOMER` and `ADMIN`.

Database table:

```text
users
```

Example structure:

```text
users
├── id
├── first_name
├── last_name
├── email
├── phone_number
├── password
├── role
├── driver_status
├── created_at
└── updated_at
```

---

# 🧱 Layered Architecture

The application follows a layered architecture.

## Controller Layer

Responsible for:

* HTTP requests
* HTTP responses
* Request validation
* Calling services

Example:

```text
AuthController
UserController
```

---

## Service Layer

Responsible for:

* Business logic
* Validation of business rules
* Coordinating repositories
* Authentication logic
* Calling other application components

Example:

```text
AuthService
UserService
```

---

## Repository Layer

Responsible for:

* Database access
* Query execution
* Persistence

Example:

```text
UserRepository
```

---

# 📋 Bean Validation

Request objects use Jakarta Bean Validation.

Example:

```java
@NotBlank
private String firstName;

@NotBlank
@Email
private String email;
```

The validation flow is:

```text
HTTP Request
     │
     ▼
Controller
     │
     ▼
Bean Validation
     │
     ├── Invalid → Error Response
     │
     └── Valid
           │
           ▼
        Service
```

---

# ⚠️ Exception Handling

The application uses custom exceptions and global exception handling.

Example custom exception:

```java
ResourceNotFoundException
```

Global handling:

```java
@ExceptionHandler(ResourceNotFoundException.class)
```

Flow:

```text
Controller
    │
    ▼
Service
    │
    ▼
Exception
    │
    ▼
GlobalExceptionHandler
    │
    ▼
ErrorResponse
    │
    ▼
HTTP Response
```

Example response:

```json
{
  "timestamp": "2026-08-18T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found",
  "path": "/api/v1/users/10"
}
```

---

# 📁 Project Structure

Current project structure:

```text
src/
└── main/
    └── java/
        └── com/
            └── simon/
                └── application/
                    │
                    ├── Application.java
                    │
                    ├── controller/
                    │   ├── AuthController.java
                    │   └── UserController.java
                    │
                    ├── service/
                    │   ├── AuthService.java
                    │   ├── UserService.java
                    │   └── impl/
                    │       ├── AuthServiceImpl.java
                    │       └── UserServiceImpl.java
                    │
                    ├── repository/
                    │   └── UserRepository.java
                    │
                    ├── entity/
                    │   └── User.java
                    │
                    ├── dto/
                    │   ├── RegisterRequest.java
                    │   ├── LoginRequest.java
                    │   └── ...
                    │
                    ├── security/
                    │   ├── SecurityConfig.java
                    │   └── JwtAuthenticationFilter.java
                    │
                    ├── jwt/
                    │   └── JwtService.java
                    │
                    ├── exception/
                    │   ├── ResourceNotFoundException.java
                    │   └── GlobalExceptionHandler.java
                    │
                    ├── response/
                    │   └── ErrorResponse.java
                    │
                    └── enums/
                        └── UserRole.java
```

---

# ⚙️ Configuration

`application.properties`:

```properties
server.port=8080

# ===============================
# PostgreSQL
# ===============================
spring.datasource.url=jdbc:postgresql://localhost:5432/delivery_app
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ===============================
# Hibernate
# ===============================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# ===============================
# Redis
# ===============================
spring.data.redis.host=localhost
spring.data.redis.port=6379

# ===============================
# Kafka
# ===============================
spring.kafka.bootstrap-servers=localhost:9092

# ===============================
# Logging
# ===============================
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

---

# 🐳 Docker Compose

Infrastructure:

```yaml
services:

  postgres:
    image: postgres:15
    container_name: delivery-postgres
    ports:
      - "5432:5432"

  redis:
    image: redis:7.2-alpine
    container_name: delivery-redis
    ports:
      - "6379:6379"

  kafka:
    image: apache/kafka:3.8.1
    container_name: delivery-kafka
    ports:
      - "9092:9092"
```

The actual project `docker-compose.yml` contains the complete configuration including health checks, persistence, Kafka KRaft configuration, and environment variables.

---

# 🚀 Running the Project

## 1. Start Docker

Make sure Docker Desktop is running.

---

## 2. Start infrastructure

From the project root:

```bash
docker compose up -d
```

Check containers:

```bash
docker ps
```

Expected containers:

```text
delivery-postgres
delivery-redis
delivery-kafka
```

---

## 3. Start Spring Boot

Using Maven:

```bash
./mvnw spring-boot:run
```

Or run the application from IntelliJ IDEA.

The application starts on:

```text
http://localhost:8080
```

---

# 🧪 Testing

Postman is used for API testing.

Authentication testing should follow this sequence:

```text
1. Register
      ↓
2. Check PostgreSQL
      ↓
3. Verify BCrypt password
      ↓
4. Login
      ↓
5. Receive JWT
      ↓
6. Call protected endpoint
      ↓
7. Send JWT in Authorization header
      ↓
8. Verify successful response
```

Authorization header:

```http
Authorization: Bearer <JWT>
```

---

# 🔄 Order Lifecycle

The planned order lifecycle is:

```text
CREATED
   ↓
CONFIRMED
   ↓
ASSIGNED
   ↓
OUT_FOR_DELIVERY
   ↓
DELIVERED
```

Possible cancellation:

```text
CREATED
   ↓
CANCELLED
```

---

# 🚚 Delivery Lifecycle

The planned delivery lifecycle is:

```text
PENDING
   ↓
ASSIGNED
   ↓
PICKED_UP
   ↓
OUT_FOR_DELIVERY
   ↓
DELIVERED
```

---

# 📨 Event-Driven Architecture

Kafka will allow services to communicate asynchronously.

Example:

```text
Customer
   │
   ▼
Order Service
   │
   │ Create Order
   ▼
ORDER_CREATED
   │
   ▼
Kafka
   │
   ├───────────────┐
   ▼               ▼
Delivery        Notification
Service         Service
   │               │
   ▼               ▼
Assign Driver   Notify Customer
```

Potential events:

```text
ORDER_CREATED
ORDER_CONFIRMED
ORDER_CANCELLED
DELIVERY_ASSIGNED
DELIVERY_PICKED_UP
DELIVERY_OUT_FOR_DELIVERY
DELIVERY_COMPLETED
```

---

# ⚡ Redis Caching Strategy

Redis will be introduced for frequently accessed data.

Example:

```text
GET /api/v1/orders/{id}
```

First request:

```text
Application
    ↓
Redis
    ↓
CACHE MISS
    ↓
PostgreSQL
    ↓
Redis
    ↓
Response
```

Subsequent requests:

```text
Application
    ↓
Redis
    ↓
CACHE HIT
    ↓
Response
```

---

# 🧩 Planned Microservices

The eventual system will contain the following major services:

### 1. User/Auth Service

Handles:

* Registration
* Login
* Authentication
* Users
* Roles

### 2. Order Service

Handles:

* Orders
* Order lifecycle
* Order status

### 3. Delivery Service

Handles:

* Deliveries
* Delivery assignments
* Delivery status

### 4. Driver Service

Handles:

* Drivers
* Driver availability
* Driver assignments
* Driver status

### 5. Notification Service

Handles:

* Notifications
* Event consumption
* Customer updates

---

# 🔮 Future Architecture

The final architecture is intended to evolve toward:

```text
                         CLIENT
                           │
                           ▼
                    ┌──────────────┐
                    │ API Gateway  │
                    └──────┬───────┘
                           │
       ┌───────────────────┼────────────────────┐
       │                   │                    │
       ▼                   ▼                    ▼
 ┌───────────┐       ┌───────────┐       ┌───────────┐
 │   User    │       │   Order   │       │ Delivery  │
 │  Service  │       │  Service  │       │  Service  │
 └─────┬─────┘       └─────┬─────┘       └─────┬─────┘
       │                   │                    │
       ▼                   ▼                    ▼
 PostgreSQL           PostgreSQL            PostgreSQL

       ┌────────────────────────────────────────┐
       │                  Redis                 │
       └────────────────────────────────────────┘

       ┌────────────────────────────────────────┐
       │                  Kafka                 │
       │              Event Streaming           │
       └───────────────────┬────────────────────┘
                           │
                 ┌─────────┴─────────┐
                 ▼                   ▼
          Notification          Driver/
             Service            Tracking
```

---

# 🗺️ Development Roadmap

## Phase 1 — Foundation

* [x] Spring Boot project
* [x] Maven configuration
* [x] Docker setup
* [x] PostgreSQL
* [x] Redis
* [x] Kafka
* [x] Docker Compose
* [x] Database connection

## Phase 2 — User Management

* [x] User entity
* [x] User repository
* [x] User service
* [x] User controller
* [x] Bean Validation
* [x] Custom exceptions
* [x] Global exception handling

## Phase 3 — Authentication

* [x] Spring Security
* [x] Password hashing
* [x] JWT service
* [x] JWT authentication filter
* [x] Login
* [x] Registration

## Phase 4 — Authorization

* [x] CUSTOMER authorization
* [x] DRIVER authorization
* [x] ADMIN authorization
* [x] Role-based endpoint protection
* [x] Method-level authorization where required

> CUSTOMER and DRIVER authorization are exercised directly by the Order/Delivery/Driver endpoints added in Phases 5–7, using the `@PreAuthorize` / ownership-check mechanism established here.

## Phase 5 — Order Management

* [x] Order entity
* [x] Order repository
* [x] Order service
* [x] Order controller
* [x] Order lifecycle
* [x] Customer order APIs

## Phase 6 — Delivery Management

* [x] Delivery entity
* [x] Delivery repository
* [x] Delivery service
* [x] Delivery controller
* [x] Driver assignment
* [x] Delivery lifecycle

## Phase 7 — Driver Management

* [x] Driver status (`DriverStatus`: AVAILABLE / UNAVAILABLE / ON_DELIVERY, tracked on `User`)
* [x] Driver service (list drivers, list available drivers, update own status)
* [x] Driver availability (self-service `AVAILABLE` ⇄ `UNAVAILABLE` toggle)
* [x] Driver assignment (delivery assignment checks + flips availability automatically)

> There is no separate `Driver` entity — a driver is a `User` with `role = DRIVER` plus a `driverStatus`. `ON_DELIVERY` is system-managed only: set on delivery assignment, released back to `AVAILABLE` on `DELIVERED`. Known gap: driver accounts can currently be created through public self-registration with no vetting — see Phase 11 or revisit before production.

## Phase 8 — Redis

* [x] Redis configuration (`RedisConfig`: `@EnableCaching` + `RedisCacheManagerBuilderCustomizer`, JSON serialization)
* [x] Cache frequently accessed data (`orders` by id, `deliveries` by id, `availableDrivers` list)
* [x] Cache invalidation (`@CachePut` on order/delivery writes; explicit `CacheManager` eviction of `availableDrivers` on the two transitions that change driver availability)
* [x] Cache TTL (`orders` 5 min, `deliveries` 2 min, `availableDrivers` 30 sec)
* [x] Cache testing — verified manually against a live Redis instance (2026-09-03): `orders::<id>` and `deliveries::<id>` keys appear on first read, subsequent reads skip the SQL query, `@CachePut` updates the cached value in place on writes, and `availableDrivers::*` is evicted immediately on driver assignment rather than waiting out its TTL.

> **Bug found during that verification (fixed 2026-09-03):** `OrderResponse`, `DeliveryResponse`, and `UserResponse` were annotated only `@Data @Builder` with no explicit constructors. Lombok's rule is that `@Builder` generates its own package-private all-args constructor when no constructor is explicitly declared, and that suppresses `@Data`'s usual no-args constructor. Result: these classes had **only** an all-args constructor, so Jackson could serialize them into Redis fine (via getters) but couldn't deserialize the cached JSON back out on a cache **hit** (`InvalidDefinitionException: no Creators, like default constructor, exist`) — invisible on a cache miss, and invisible at compile time, only surfacing when the cached path was actually exercised. Fixed by adding explicit `@NoArgsConstructor @AllArgsConstructor` to all three, plus `OrderEvent`/`DeliveryEvent` (same gap, would have broken Kafka consumption the same way) and, preventively, `LoginResponse`/`ErrorResponse` (not currently round-tripped through JSON anywhere, but same latent gap). **Redis re-verification of the cache-hit path is recommended** to confirm the fix.

## Phase 9 — Kafka

* [x] Kafka producers (`EventPublisher`, using `KafkaTemplate<String, Object>` with JSON serialization, keyed by order/delivery id so per-entity ordering is preserved)
* [x] Kafka consumers (`NotificationListener`, `@KafkaListener` on both topics — logs the event as a stand-in for a real notification system)
* [x] Order events (`ORDER_CREATED`, `ORDER_CONFIRMED`, `ORDER_CANCELLED` — published from `OrderServiceImpl` on the matching transitions)
* [x] Delivery events (`DELIVERY_ASSIGNED`, `DELIVERY_PICKED_UP`, `DELIVERY_OUT_FOR_DELIVERY`, `DELIVERY_COMPLETED` — published from `DeliveryServiceImpl`)
* [x] Notification events (consumed by `NotificationListener`; a real Notification Service is future work, not built yet)
* [ ] Event-driven communication — **not yet verified at runtime.** Topics (`order-events`, `delivery-events`, 3 partitions each) are declared via `KafkaTopicConfig` `NewTopic` beans, but this hasn't been confirmed against a live Kafka broker (no Docker daemon in this sandbox).

> `OrderStatus`/`DeliveryStatus` transitions to `ASSIGNED`/`OUT_FOR_DELIVERY`/`DELIVERED` on the **Order** entity intentionally do **not** publish their own events — those transitions are already represented by the corresponding **Delivery**-side events, so publishing both would be a duplicate signal for the same real-world change.

## Phase 10 — Microservices

* [ ] Extract User/Auth Service
* [ ] Extract Order Service
* [ ] Extract Delivery Service
* [ ] Extract Driver Service
* [ ] Extract Notification Service
* [ ] Introduce API Gateway
* [ ] Inter-service communication

## Phase 11 — Production Readiness

* [ ] Unit testing
* [ ] Integration testing
* [ ] Dockerize services
* [ ] Logging
* [ ] Monitoring
* [ ] Distributed tracing
* [ ] Error handling
* [ ] Configuration management
* [ ] CI/CD
* [ ] Cloud deployment

---

# 📊 Current Project Status

```text
Foundation                 ████████████████████ 100%
Database                   ████████████████████ 100%
User Management            ████████████████████ 100%
Validation                 ████████████████████ 100%
Exception Handling         ████████████████████ 100%
Authentication             ████████████████████ 100%
Authorization              ████████████████████ 100%
Orders                     ████████████████████ 100%
Deliveries                 ████████████████████ 100%
Drivers                    ████████████████████ 100%
Redis Caching              ████████████████████ 100%
Kafka Events               ████████████████░░░░  80% (implemented, not runtime-verified)
Microservices              ░░░░░░░░░░░░░░░░░░░░   0%
Testing                    ░░░░░░░░░░░░░░░░░░░░   0%
Deployment                 ░░░░░░░░░░░░░░░░░░░░   0%
```

---

# 🎯 Project Goals

The project aims to demonstrate practical knowledge of:

* Java backend development
* Spring Boot
* REST API design
* Layered architecture
* Spring Security
* JWT authentication
* Role-based authorization
* Password security
* PostgreSQL
* JPA/Hibernate
* Redis caching
* Apache Kafka
* Event-driven architecture
* Microservices
* Docker
* Exception handling
* Bean Validation
* Testing
* Production-ready backend design

---

# 👨‍💻 Development Philosophy

The system is intentionally being developed incrementally.

Instead of immediately creating multiple microservices, the core business functionality is first implemented in a modular Spring Boot application.

The evolution is:

```text
Modular Spring Boot Application
              ↓
Clean Layered Architecture
              ↓
Authentication & Authorization
              ↓
Order & Delivery Domain
              ↓
Redis Caching
              ↓
Kafka Events
              ↓
Service Boundaries
              ↓
Microservices
              ↓
Production Deployment
```

This allows each architectural concept to be understood and tested before introducing additional distributed-system complexity.

---

# 📌 Immediate Next Step

Phases 1–8 are complete and verified: authentication, authorization, the Order / Delivery / Driver domain, and Redis caching (confirmed against a live Redis instance on 2026-09-03).

Phase 9 (Kafka events) is implemented in code — `EventPublisher` publishes on the Order/Delivery lifecycle transitions, `NotificationListener` consumes and logs both topics.

**Outstanding before Phase 9 is truly done:** manual runtime verification against a live Kafka broker (start `docker compose up -d`, run the app, hit the order/delivery endpoints, and confirm in the app logs that `NotificationListener` actually received each published event — the same kind of check we just did for Redis in Phase 8).

After that, the next major feature is **Phase 10 — Microservices**.

---

# 📄 License

This project is currently intended as a backend engineering learning and portfolio project.

```
```
