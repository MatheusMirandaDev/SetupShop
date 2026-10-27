# SetupShop Agent Guide

## Project

SetupShop is a modular monolith REST API for e-commerce management. It uses
Java 21, Spring Boot 4, Spring Web MVC, Spring Data JPA/Hibernate, Jakarta
Validation, PostgreSQL 17, Flyway, Docker Compose, Maven Wrapper, Lombok,
OpenAPI/Swagger, JUnit, Mockito, and Testcontainers.

## Architecture

- Organize code by business feature, such as `product`, `customer`, and `order`.
- `domain`: entities, invariants, exceptions, and technology-independent
  repository contracts.
- `application/usecase`: commands and use cases that coordinate domain
  operations.
- `infrastructure/persistence`: Spring Data repositories and adapters.
- `infrastructure/web`: controllers, DTOs, request validation, and HTTP errors.
- `shared`: only genuinely reusable configuration, pagination, and HTTP types.
- Keep framework and HTTP concerns out of domain repository contracts and
  application-owned pagination types.

## Code and tests

- Use the English names and messages already established by the codebase.
- Format Java with the IntelliJ IDEA built-in formatter using the project
  `.editorconfig`.
- Format targeted Java files only; never broadly format `src/main`.
- Prefer constructor injection, records for commands and DTOs, and `BigDecimal`
  for monetary values.
- Keep business invariants in domain methods.
- For multi-field updates, validate every candidate value before assigning any
  field.
- Keep web validation, domain validation, and database constraints consistent
  without overlapping validations unnecessarily.
- Mirror production packages under `src/test/java`.
- Use unit tests for domain and use cases, MockMvc for web tests, and
  Testcontainers with PostgreSQL and Flyway for persistence integration tests.

## Test commands

Docker must be running for persistence integration tests.

```bash
./mvnw test
./mvnw -Dtest=ClassName test
./mvnw -Dtest=ClassName#methodName test
```

## Continuous integration

- `.github/workflows/ci.yml` runs on pushes and pull requests.
- It sets up Java 21 and runs `./mvnw test`, including persistence
  integration tests with Testcontainers and PostgreSQL. The runner needs
  Docker for those tests.

## Before a commit

1. Run the relevant tests and the full suite when integration may be affected.
2. Inspect `git status --short` and preserve unrelated changes.
3. Stage named files instead of using broad staging.
4. Review `GIT_PAGER=cat git diff --cached`.
5. Run `git diff --cached --check`.
6. Use a focused Conventional Commit.
7. Do not commit or push unless the user requests it.

## Protected and sensitive files

- Never commit `.env`; track only non-secret examples in `.env.example`.
- Never edit an applied Flyway migration; create the next versioned migration.
- Do not broadly format migrations because that changes Flyway checksums.
- Do not stage generated or local files such as `target/`, `.idea/`, or
  `.attach_pid*`.
- Preserve unrelated working-tree changes and Maven Wrapper line endings.

## Current scope

- Product supports creation, lookup by ID, paginated listing, partial update,
  and logical deactivation. Inactive products remain consultable but are not
  available for new purchases.
- Customer is a business entity, separate from Account. It
  supports creation, lookup by ID, paginated listing, atomic partial update,
  and logical deactivation. Its current rules include required normalized name
  and email, unique lowercase email, and a required unformatted 11-digit phone.
- Continue as a modular monolith and deliver changes in small vertical slices.
- Order placement is implemented through `POST /orders`. It creates an order
  when an active customer confirms one or more active products with positive
  quantities, before payment. Each OrderItem records the product's unit price
  at placement; subtotals and the stored order total use `BigDecimal`. The
  initial status is `CREATED`, and later catalog price changes do not alter
  placed orders. `GET /orders/{id}` retrieves a placed order with its items,
  and `GET /orders` lists order summaries with pagination. Other order
  operations are not implemented.
- Account domain and persistence are implemented on `feat/customer-registration`.
  Account stores a normalized login email, a supplied password hash, a role,
  and a Customer ID required for the `CUSTOMER` role. Login emails and non-null
  Customer IDs are unique across Accounts. PostgreSQL generates Account
  creation and update timestamps. Unit and persistence integration tests cover
  domain rules, storage, email uniqueness, the unique Customer link, and the
  Customer foreign key.
- The registration use case and endpoint, password hashing configuration,
  authentication, and authorization remain unimplemented. Customer email
  changes do not yet synchronize Account email. The adopted registration and
  Order access rules are in `docs/decisions.md`.
- Cart, inventory workflows, payments, messaging, cloud deployment, and
  microservices remain deferred until required by their slice.

## Mentoring workflow for customer registration

- Give one small exercise at a time, starting with a test. Wait for the user's
  implementation, review it without changing their files, and let them make
  corrections before validating and advancing. Provide the proposed test name.
- Do not implement exercises for the user unless they explicitly request it.
  Do not commit or push without an explicit request.
- The first slice, Account domain and persistence, is complete and reviewed.
  The user reported a successful full suite with 180 tests. No commit or push
  has been requested. Inform the user when a slice is ready for commit.
- The next planned slice is password hashing before the registration use case.
  Choose the encoder before proposing its first small test exercise. Account
  continues to receive a hash already calculated outside the entity.

## Open customer registration decisions

- Define how Customer email changes keep the login email stored on Account
  consistent in the same operation. The limit of one Account per Customer and
  unique Account login emails are already adopted.
- Define the registration endpoint and response, including the HTTP contract
  for an email already used by an Account and the behavior for an email that
  belongs to a Customer without an Account. Duplicate Account emails must be
  rejected, but the registration HTTP response remains to be defined.
- Decide how existing Customers without accounts obtain access and how their
  ownership is verified before an Account is linked.
- Decide how administrator accounts are created and whether they have a
  Customer record.
- Decide whether deactivating a Customer disables its Account.
- Choose the password encoder and login mechanism in their respective slices;
  decide when to add email verification and password recovery.
- Define permissions for Product and Customer endpoints, including the current
  `POST /customers` and Customer email changes.
- Choose the HTTP response for a logged-in customer requesting another
  customer's order while preserving the agreed ownership rule.
