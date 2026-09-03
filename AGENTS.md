# SetupShop Agent Guide

## Project

SetupShop is a modular-monolith REST API for e-commerce management. It uses
Java 21, Spring Boot 4, Spring Web MVC, Spring Data JPA/Hibernate, Jakarta
Validation, PostgreSQL 17, Flyway, Docker Compose, Maven Wrapper, Lombok,
OpenAPI/Swagger, JUnit, Mockito, and Testcontainers.

## Architecture

- Organize code by business feature, such as `product` and `customer`.
- `domain`: entities, invariants, exceptions, and technology-independent
  repository contracts.
- `application/usecase`: commands and use cases that coordinate domain
  operations.
- `infrastructure/persistence`: Spring Data repositories and adapters.
- `infrastructure/web`: controllers, DTOs, request validation, and HTTP errors.
- `shared`: only genuinely reusable configuration and pagination types.
- Keep framework and HTTP concerns out of domain repository contracts and
  application-owned pagination types.

## Code and tests

- Use the English names and messages already established by the codebase.
- Format Java with Palantir Java Format and 4-space indentation.
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
- Customer is a business entity, separate from any future login account. Its
  current rules include required normalized name and email, unique lowercase
  email, and a required unformatted 11-digit phone.
- Continue as a modular monolith and deliver changes in small vertical slices.
- Orders, inventory workflows, authentication, payments, messaging, cloud
  deployment, and microservices remain deferred until required by their slice.