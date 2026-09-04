# Phase 10 — Microservices Architecture

This document is the design reference for splitting the Delivery App monolith (Phases 1–9) into independently deployable services. It was worked out before any extraction code was written, and each extraction step should be checked against it.

## Why this split, and the shape it takes

Phases 1–9 built one Spring Boot application: auth, the Order/Delivery/Driver domain, Redis caching, and Kafka events, all verified against live infrastructure. Splitting that into services is a different kind of change from anything before it — it's not new code inside one app, it's dividing the app itself, which forces real distributed-systems decisions that simply don't exist in a monolith: who owns which data, how services find out about things they don't own, and what level of consistency each interaction actually needs.

**Core decisions:**
- **5 services** — User/Auth, Driver, Order, Delivery, Notification. Driver gets its own service and its own `drivers` table rather than staying a column on `User`: a real Driver bounded context (vehicle info, documents, ratings, location, earnings later) doesn't belong in an identity/auth table, and splitting it out now avoids a second migration later.
- **One Postgres container per service that needs one** (user, driver, order, delivery — 4 total) — real data-ownership boundaries, not shared schemas.
- **One shared Redis and one shared Kafka broker** across all services — these aren't data-ownership boundaries the way a service's own table is. Redis cache names and Kafka topics are already logically namespaced, so sharing the instance is safe and avoids pointless infra duplication.
- **Driver status sync is event-driven, not a synchronous cross-service call** — Driver Service consumes `delivery-events` and updates status itself in reaction to `DELIVERY_ASSIGNED`/`DELIVERY_COMPLETED`, rather than Delivery Service reaching into Driver Service's data to mutate it directly.
- **Event contracts and JWT-handling code are duplicated per service, not shared as a library** — each service owns its own copy of the event POJOs and the `JwtService`/`JwtAuthenticationFilter`/`SecurityConfig`/`UserPrincipal` classes it needs. No shared Maven module, no coordinated-deploy coupling between services.

## How Driver Service learns a user is a driver

User Service owns the only copy of `role`. Rather than Driver Service calling User Service on every check, User Service publishes a **`USER_REGISTERED`** event (new topic: `user-events`) on every user creation — self-registration and admin-created — carrying `userId`, `firstName`, `lastName`, `email`, `role`. Driver Service consumes it and, only when `role == DRIVER`, creates its own driver row.

This means Driver Service's `drivers` table denormalizes a name/email **snapshot taken at registration time**, so `/api/v1/drivers` and `/api/v1/drivers/available` can respond without a live call back to User Service.

**Known, accepted gap:** if a driver later updates their name/email via User Service, Driver Service's snapshot goes stale unless User Service also publishes a `USER_UPDATED` event. Not handled in this pass — deliberately out of scope, flagged here rather than silently ignored.

**Identity/key choice:** Driver Service's `drivers` table uses the **same id as `users.id`** as its own primary key (a 1:1 "extension table" pattern), not a separate auto-generated driver id. Every existing contract that already refers to "driverId" (`AssignDriverRequest.driverId`, `DeliveryResponse.driverId`, `getDeliveriesForDriver(driverId)`) keeps meaning exactly what it means today — just resolved against Driver Service's table instead of User Service's. No dual-id translation layer needed anywhere else.

## The 5 services + gateway

| Service | Owns (DB) | External API (via gateway) | Internal API (service-to-service) | Kafka |
|---|---|---|---|---|
| **user-service** | `users` table (id, firstName, lastName, email, phoneNumber, password, role, timestamps) — **no `driverStatus` column anymore** | `/api/v1/auth/**`, `/api/v1/users/**` (today's `AuthController`, `UserController` move here; `DriverController` moves to driver-service instead) | `GET /internal/users/{id}` — minimal DTO (`id`, `role`) for order-service to validate a customer reference | **Produces** `user-events` (new topic; `USER_REGISTERED` on every user creation) |
| **driver-service** | `drivers` table (id = same value as `users.id`, firstName, lastName, email — snapshot at registration, driverStatus) | `/api/v1/drivers/**` (today's `DriverController` moves here unchanged) | `GET /internal/drivers/{id}` — minimal DTO (`id`, `driverStatus`) for delivery-service to validate at assignment time | **Consumes** `user-events` (creates a driver row when `role==DRIVER`); **consumes** `delivery-events` (`DELIVERY_ASSIGNED`→`ON_DELIVERY`, `DELIVERY_COMPLETED`→`AVAILABLE`, replacing today's inline `userRepository.save(driver)` calls in `DeliveryServiceImpl`) |
| **order-service** | `orders` table, with `customerId: Long` replacing today's `@ManyToOne User customer` | `/api/v1/orders/**` (today's `OrderController` moves here unchanged) | `GET /internal/orders/{id}` — minimal DTO (`id`, `status`) for delivery-service to validate at delivery-creation time; **calls** user-service's `GET /internal/users/{id}` at order-creation time to validate the customer exists | **Produces** `order-events` (unchanged from today's `EventPublisher`/`OrderEvent`) |
| **delivery-service** | `deliveries` table, with `orderId: Long` / `driverId: Long` replacing today's `@ManyToOne`/`@OneToOne` relations | `/api/v1/deliveries/**` (today's `DeliveryController` moves here unchanged) | none exposed; **calls** order-service (validate order exists/confirmed at delivery creation) and **driver-service** (validate driver exists and `driverStatus=AVAILABLE` at assignment time, via `GET /internal/drivers/{id}`) | **Produces** `delivery-events` (unchanged); no longer mutates another service's data directly — driver status sync is driver-service's own job, reacting to this same event |
| **notification-service** | none (stateless consumer) | none | none | **Consumes** `order-events` + `delivery-events` — today's `NotificationListener`, moved as-is into its own minimal Spring Boot app |
| **api-gateway** | none | routes all of the above (`/api/v1/auth/**`, `/api/v1/users/**` → user-service; `/api/v1/drivers/**` → driver-service; `/api/v1/orders/**` → order-service; `/api/v1/deliveries/**` → delivery-service) | — | — |

**Implementation note (deviation from the original plan):** the gateway is built on Spring Cloud Gateway's **WebFlux** implementation (`spring-cloud-starter-gateway-server-webflux`), not the newer MVC-based variant — this is otherwise a purely blocking/servlet-stack project, so the MVC-based gateway would have been the more consistent choice, but only the WebFlux variant was available in this environment's offline Maven cache at build time. This is invisible in practice: the gateway is pure declarative route configuration (`application.properties`), no reactive Java code was written. Routes use the plain `spring.cloud.gateway.routes[N]` properties (verified directly against the cached jar's `spring-configuration-metadata.json` rather than assumed from memory).

**Why most external API contracts don't need to change:** `OrderResponse`/`DeliveryResponse` already only expose flat ids (`customerId`, `orderId`, `driverId`) rather than nested objects, so only the entity layer underneath (JPA relations → plain `Long` columns) and the internal validation logic (in-process repository lookup → HTTP call to the owning service) change. `UserResponse` loses its `driverStatus` field (moves to a `DriverResponse` shape returned by driver-service instead) — the one visible external-contract change from this split.

### Which cross-service calls are synchronous vs asynchronous, and why

| Interaction | Mechanism | Why |
|---|---|---|
| New user registers → Driver Service (and anyone else) finds out | **Async** (`USER_REGISTERED`) | Fact-propagation. Registration must succeed even if Driver Service happens to be down. |
| Create order → validate customer exists | **Sync** (`GET /internal/users/{id}`) | A live gate before an important action; cheap and low-frequency, no value in keeping a local user cache just for this. |
| Create delivery → validate order exists/confirmed | **Sync** (`GET /internal/orders/{id}`) | Same reasoning — must know right now whether creating this delivery is valid. |
| Assign driver → validate driver exists & `AVAILABLE` | **Sync** (`GET /internal/drivers/{id}`) | The one call in the system that's genuinely "about to make an irreversible decision" — needs a live answer at that instant. |
| Order/Delivery lifecycle events → Notification Service, Driver Service | **Async** (`order-events`/`delivery-events`) | Broadcast/fan-out to independent consumers without the producer needing to know who's listening. |
| Ordinary reads (`GET order/{id}`, `GET delivery/{id}`, `GET drivers/available`) | **No call at all** | Because the async paths above already replicate what each service needs locally, most reads never cross a service boundary. |

The rule of thumb: **a gate you must pass before acting is synchronous; an announcement of something that already happened is asynchronous.**

### Auth handling

The gateway is a pure router — it does **not** validate JWTs itself. Each of the 4 services with an external API keeps its own copy of `SecurityConfig` / `JwtAuthenticationFilter` / `JwtService` / `UserPrincipal`, validating the JWT independently using the same shared `jwt.secret`. This avoids inventing a new trust/header-forwarding model, and is the same "duplicate, don't share a library" principle applied to the event DTOs.

**Important distinction:** unlike the JWT-handling *code*, which is fine to duplicate independently per service, the `jwt.secret` *value* itself must be byte-for-byte identical across every service that validates tokens — JWT verification recomputes an HMAC signature using this key, so a token signed by user-service with one secret will fail verification anywhere the secret differs. This is shared *configuration*, not shared *code*; in a real deployment it should come from one shared secret manager/vault entry, not copy-pasted properties files that can drift.

## Project layout

Independent, standalone Maven projects (own `pom.xml`, own `Application` class) as sibling directories — not a multi-module reactor, matching how these would actually be deployed independently:

```
Delivery_app/
├── user-service/         (repurposed from today's root project: trimmed of Order/Delivery/Driver code)
├── driver-service/       (new: DriverController/DriverService extracted + a new `drivers` table)
├── order-service/        (new: Order entity/service/controller extracted, seeded from today's code)
├── delivery-service/     (new: Delivery entity/service/controller extracted, seeded from today's code)
├── notification-service/ (new: just NotificationListener + its own copy of the event POJOs)
├── api-gateway/          (new: Spring Cloud Gateway, routing config only)
└── docker-compose.yml    (postgres-user, postgres-driver, postgres-order, postgres-delivery, redis, kafka)
```

Port allocation: gateway `8080` (external), user-service `8081`, driver-service `8082`, order-service `8083`, delivery-service `8084`, notification-service `8085` (internal only, no external HTTP needed).

Postgres host ports (each service's own container, per the root `docker-compose.yml`): postgres-user `5432`, postgres-driver `5433`, postgres-order `5434`, postgres-delivery `5435`. Redis (`6379`) and Kafka (`9092`) are single shared instances, one each, used by every service.

## Sequencing

This is too large to do in one pass. Each step below is its own unit of work, verified independently before moving to the next:

1. **Write this design into the repo** — this document, linked from the README's Phase 10 section. *(done)*
2. **Extract user-service** — trim today's project down to User/Auth (drop driver-specific code entirely), remove `driverStatus` from `User`, add the `/internal/users/{id}` endpoint, add the `USER_REGISTERED` event publish on creation, own Postgres, verify standalone.
3. **Extract driver-service** — new `drivers` table/entity, consume `user-events` (create a driver row on `USER_REGISTERED` + `role==DRIVER`), consume `delivery-events` (flip status), move `DriverController`/`DriverService` here, add the `/internal/drivers/{id}` endpoint, own Postgres, verify standalone (registering a DRIVER in user-service results in a row appearing here).
4. **Extract order-service** — move `Order`, change `customer` relation to `customerId`, add the outbound call to user-service's internal endpoint, add `/internal/orders/{id}`, own Postgres, verify order creation still validates the customer and still publishes `order-events` correctly.
5. **Extract delivery-service** — move `Delivery`, change `order`/`driver` relations to `orderId`/`driverId`, add outbound calls to order-service and driver-service, own Postgres, verify assignment/status-transition flows and that driver status now updates via the Kafka path in driver-service instead of an in-process call.
6. **Extract notification-service** — smallest step, just move `NotificationListener` + its own event POJO copies into a new minimal project.
7. **Add api-gateway**, wire routing, bring the whole multi-service `docker-compose.yml` up together, and do an end-to-end pass (see Verification below).

## Verification

- After each service extraction (steps 2–6): that service's own `mvn compile`/`mvn test` passes standalone, and a manual smoke test against just that service confirms its existing endpoints still behave the same as before extraction.
- After step 7 (full system): the same end-to-end flow already used to verify Phases 8–9, but run against `docker-compose up` bringing up all services + 4 Postgres + shared Redis/Kafka, confirming:
  - Each service's Postgres only contains that service's own tables (proves the data-ownership split actually happened).
  - Registering a new `role=DRIVER` user causes a `drivers` row to appear in driver-service's own database (proves the `USER_REGISTERED` → Driver Service path works).
  - The synchronous validation calls (order→user, delivery→order, delivery→driver) reject invalid ids/statuses the same way today's in-process checks do.
  - The async driver-status-flip (delivery-events → driver-service) is observable: assign a driver, confirm status is `ON_DELIVERY` shortly after (not instantly — accepted eventual-consistency window), then complete the delivery and confirm it flips back to `AVAILABLE`.

## Accepted tradeoffs and known gaps

These are deliberate, discussed decisions — not oversights:

- **Double-booking race on driver assignment.** Delivery Service's synchronous check ("is this driver `AVAILABLE`") and Driver Service's eventual status flip after assignment leave a real window where two near-simultaneous assignment requests could both read a driver as `AVAILABLE` before the first assignment's event is consumed. Full prevention needs a reservation/locking mechanism — out of scope here.
- **Driver Service's name/email snapshot can go stale.** Taken once at `USER_REGISTERED` time; no `USER_UPDATED` event exists yet to refresh it if the user edits their profile later in User Service.
