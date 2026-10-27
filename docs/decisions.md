# Technical Decisions

This document records only technical decisions already adopted by SetupShop.

## Modular monolith organized by feature

- **Context:** Product, Customer, Order, and Account belong to the same e-commerce
  application and currently share infrastructure and deployment.
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

## Atomic partial updates

- **Context:** A partial update containing one valid and one invalid field must
  not leave the entity partially modified.
- **Decision:** Validate all candidate values before assigning any updated
  Product or Customer field. Validate Customer email uniqueness before changing
  the entity.
- **Consequence:** Failed updates preserve the complete previous entity state
  and are not saved.

## Logical product deactivation

- **Context:** A product may be unavailable because it has not launched or is
  out of stock, while its information must remain consultable.
- **Decision:** `DELETE /products/{id}` sets `active` to `false` instead of
  physically deleting the product.
- **Consequence:** Inactive products remain queryable but are unavailable for
  new purchases.

## Logical customer deactivation

- **Context:** Customer records must remain available for consultation and
  future business history even when the customer is no longer active.
- **Decision:** `DELETE /customers/{id}` sets `active` to `false` instead of
  physically deleting the customer.
- **Consequence:** Inactive customers remain queryable while their records are
  preserved.

## Framework-independent pagination

- **Context:** Pagination belongs to the application contract, while
  `PageRequest` and `Page` belong to Spring Data.
- **Decision:** Use `PageQuery` and `PageResult<T>` outside Spring, converting
  them in the persistence adapter and exposing `PageResponse<T>` over HTTP.
- **Consequence:** Application and domain contracts do not depend on Spring
  Data. Pages are zero-based, size is limited to 1–100, and Product, Customer,
  and Order listings are sorted by ascending ID.

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
- **Decision:** Keep `Customer` separate from `Account`.
  Normalize required names and emails, store emails in lowercase with a unique
  database constraint, and store phone numbers as exactly 11 unformatted ASCII
  digits.
- **Consequence:** Authentication concerns stay outside the Customer model, and
  customer contact data has a consistent persisted representation.

## Java formatting

- **Context:** Formatting must remain consistent without introducing build
  dependencies or changing Flyway checksums.
- **Decision:** Use the IntelliJ IDEA built-in Java formatter with the project
  `.editorconfig`, 4-space indentation, LF line endings, and targeted formatting
  only.
- **Consequence:** Formatting is reproducible without adding build dependencies,
  while SQL migrations remain untouched by broad formatting operations.

## Continuous integration

- **Context:** Product, Customer, and Order checks should run automatically on
  code changes, including persistence tests against PostgreSQL and Flyway.
- **Decision:** Use `.github/workflows/ci.yml` for pushes and pull
  requests, set up Temurin Java 21, and run `./mvnw test`.
- **Consequence:** The Maven test phase runs unit, web, and persistence
  integration tests on a runner with Docker for Testcontainers. The POM
  has no separate Failsafe configuration, so the CI command is `test`.

## Order placement and historical prices

- **Context:** Catalog prices and product or customer availability can change
  after a customer places an order.
- **Decision:** Create the Order when the customer confirms the selected
  products, before payment. Require an active customer and one or more active
  products with positive quantities. Read each unit price from Product at that
  point, capture it on the order item, and calculate item subtotals and the
  order total with `BigDecimal`. Start the order in `CREATED` status.
- **Consequence:** Later catalog price changes do not alter placed orders;
  new orders use the prices current when they are placed. Inactive customers
  and products cannot be used for new orders. Cart and payment remain separate
  future slices.

## Order aggregate and persistence

- **Context:** Order items belong to a single order and must retain the price
  recorded at placement, independently of later catalog changes.
- **Decision:** Model Order as the aggregate root and OrderItem as a value
  object stored with the order. Persist the item product ID, quantity, and
  captured unit price in `order_items`; derive each subtotal and store the
  calculated total on `orders`. Generate `created_at` in PostgreSQL and manage
  both tables through Flyway migrations.
- **Consequence:** Items have no independent repository or lifecycle. Reloaded
  orders retain their recorded prices and totals, while catalog changes affect
  only orders placed afterward.

## Order lookup and listing

- **Context:** Looking up one order needs its recorded items, while a paginated
  list needs only enough data to identify and summarize each order.
- **Decision:** `GET /orders/{id}` returns the order with its items and fetches
  them with an entity graph. `GET /orders` returns paginated summaries containing
  the order ID, customer ID, status, and total, without loading each item's
  collection for the response.
- **Consequence:** The detail endpoint can return items after the repository
  session closes. The list endpoint keeps pagination on orders and avoids
  loading item collections for every result.

## Account domain and persistence

- **Context:** Credentials and permissions need storage separate from Customer
  business data before registration and authentication are implemented.
- **Decision:** Persist Account in `accounts`, introduced by Flyway migration
  `V6`, through a framework-independent `AccountRepository` contract and a JPA
  adapter. Store a generated ID, login email, supplied password hash, role,
  Customer ID, and creation and update timestamps. The domain trims and
  lowercases email, checks its format and 255-character limit, rejects missing
  or blank password hashes and missing roles, and requires positive Customer
  IDs when provided. Store roles as enum names (`CUSTOMER` or `ADMIN`). Require
  a Customer ID for `CUSTOMER` accounts. Enforce unique emails, unique Customer
  links, a Customer foreign key, and the conditional Customer requirement in
  PostgreSQL. Generate timestamps with database defaults and the existing
  update trigger function.
- **Consequence:** Each Customer can have at most one Account. The persistence
  slice accepts a hash already calculated outside Account; it does not yet
  provide registration, password hashing, or login. The nullable Customer link
  does not settle how administrator accounts are created or linked to Customers.

## Password hashing with Argon2id and pepper (planned)

- **Context:** Account persistence currently accepts a supplied hash. The next
  slice must calculate and verify password hashes before registration is
  implemented, with additional protection if only the database is compromised.
- **Decision:** Use Argon2id with a salt automatically generated by the encoder
  for each hash, plus a secret pepper. Store the salt and algorithm parameters
  in the encoded representation; a separate salt column is not needed. Generate
  the pepper with cryptographic randomness and keep it separate from the
  password database, source code, Git, and logs. Preserve the pepper across
  application restarts and deployments. Hashing and verification belong outside
  Account; the entity continues to receive only the stored representation.
- **Consequence:** The pepper adds protection when the password database is
  exposed but the secret remains protected. Its management is part of this
  slice. The composition strategy, Argon2id parameters, encoded format and
  version identification, secret provisioning and storage, and rotation and
  recovery procedures remain to be defined. Pepper loss can prevent verification
  for all affected accounts; compromise requires a planned replacement and may
  require password resets. Password recovery must verify user identity and
  remains unimplemented. No hashing or pepper configuration is implemented yet.
- **Reference:** [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

## Customer account registration (planned)

- **Context:** `Customer` represents business data, while a login account
  represents credentials and permissions. A person should enter their data
  once when registering.
- **Decision:** Keep `Customer` and `Account` as separate models. Customer
  registration accepts name, email, phone, and password, then creates both
  records in one transaction. Link the account to the customer by customer ID,
  not by email. Keep name and phone on Customer instead of duplicating them on
  Account. Assign the `CUSTOMER` role on the server and store only a password
  hash. Each Customer may have at most one Account, and login emails must be
  unique across Accounts. Registration with an email already used by an Account
  must be rejected with an indication that the email is already in use.
  CPF is outside the first registration slice. Customer registration and
  authentication precede a real payment integration.
- **Consequence:** Registration cannot leave only one of the records persisted.
  Changing a customer's email must update the email used for login in the same
  operation. Account storage is implemented. The registration use case and
  endpoints, password hashing configuration, and Customer-to-Account email
  synchronization remain to be implemented. Access for existing Customers
  without Accounts and the registration HTTP error contract remain open.

## Order access policy (planned)

- **Context:** Login alone does not establish which order an account may
  create or read.
- **Decision:** An authenticated customer may create orders only for their own
  customer ID and read or list only their own orders. An administrator may read
  any order and list all orders, but may not place one. Unauthenticated callers
  may not use Order endpoints.
- **Consequence:** Order placement must obtain the customer identity from the
  authenticated account instead of trusting a customer ID in the request.
  Order detail must check ownership, and paginated listing must filter by
  ownership before calculating page content and totals. These rules are agreed
  but are not enforced by the current API yet.
