# Technical Decisions

This document records only technical decisions already adopted by SetupShop.

## Modular monolith organized by feature

- **Context:** Product and Customer belong to the same e-commerce application
  and currently share infrastructure and deployment.
- **Decision:** Keep a modular monolith organized first by business feature.
- **Consequence:** New features remain isolated by package without introducing
  distributed-system complexity.

## Layered responsibilities inside each feature

- **Context:** Business rules must remain independent from HTTP and persistence
  frameworks.
- **Decision:** Separate `domain`, `application/usecase`, and `infrastructure`,
  with technology-independent repository contracts implemented by persistence
  adapters.
- **Consequence:** Domain and use cases can be tested without starting Spring
  or PostgreSQL.

## Validation at the appropriate boundaries

- **Context:** Invalid data may enter through HTTP, application code, or direct
  database access.
- **Decision:** Use request validation for the HTTP contract, domain methods for
  business invariants, and database constraints for persisted integrity.
- **Consequence:** Validation is layered, while overlapping constraints with
  duplicate responsibilities are avoided.

## Atomic product updates

- **Context:** A partial update containing one valid and one invalid field must
  not leave the entity partially modified.
- **Decision:** Validate all candidate values before assigning any updated
  product field.
- **Consequence:** Failed updates preserve the complete previous product state
  and are not saved.

## Logical product deactivation

- **Context:** A product may be unavailable because it has not launched or is
  out of stock, while its information must remain consultable.
- **Decision:** `DELETE /products/{id}` sets `active` to `false` instead of
  physically deleting the product.
- **Consequence:** Inactive products remain queryable but are unavailable for
  new purchases.

## Framework-independent pagination

- **Context:** Pagination belongs to the application contract, while
  `PageRequest` and `Page` belong to Spring Data.
- **Decision:** Use `PageQuery` and `PageResult<T>` outside Spring, converting
  them in the persistence adapter and exposing `PageResponse<T>` over HTTP.
- **Consequence:** Application and domain contracts do not depend on Spring
  Data. Pages are zero-based, size is limited to 1–100, and products are sorted
  by ascending ID.

## Database evolution and timestamps

- **Context:** PostgreSQL is the persistent store and Hibernate must not create
  or silently modify the schema.
- **Decision:** Manage schema evolution with append-only Flyway migrations,
  keep `ddl-auto` as `validate`, and generate timestamps with database defaults
  and an `updated_at` trigger.
- **Consequence:** Applied migrations are never edited, and persisted
  timestamps are controlled consistently by PostgreSQL.

## Persistence integration testing

- **Context:** Repository behavior, migrations, PostgreSQL constraints, and JPA
  mappings cannot be fully verified with mocks.
- **Decision:** Use Testcontainers with a real PostgreSQL instance and Flyway
  in persistence integration tests.
- **Consequence:** Persistence tests require Docker and reload entities after
  `flush()` and `clear()` when validating database-generated state.

## Customer identity and data

- **Context:** A business customer is not necessarily the account used for
  authentication.
- **Decision:** Keep `Customer` separate from any future login account.
  Normalize required names and emails, store emails in lowercase with a unique
  database constraint, and store phone numbers as exactly 11 unformatted ASCII
  digits.
- **Consequence:** Authentication concerns stay outside the Customer model, and
  customer contact data has a consistent persisted representation.

## Java formatting

- **Context:** Formatting must be consistent without changing Flyway checksums.
- **Decision:** Use Palantir Java Format with 4-space indentation and format
  targeted Java files only.
- **Consequence:** Java formatting is reproducible, while SQL migrations remain
  untouched by broad formatting commands.