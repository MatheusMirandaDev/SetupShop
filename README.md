# SetupShop

SetupShop is a REST API for managing products, customers, and orders in an
e-commerce system. It is a modular monolith built with Java 21, Spring Boot 4,
and PostgreSQL 17.

The current order flow records an order when a customer confirms the selected
products, before payment. Each item keeps the product price at that moment, so
later catalog changes do not alter placed orders.

## Contents

- [Implemented scope](#implemented-scope)
- [Getting started](#getting-started)
- [Authentication](#authentication)
- [API](#api)
- [Architecture](#architecture)
- [Data and migrations](#data-and-migrations)
- [Tests and CI](#tests-and-ci)
- [Documentation and future scope](#documentation-and-future-scope)

## Implemented scope

| Feature | Available operations |
| --- | --- |
| Products | Create, get by ID, list with pagination, partially update, and logically deactivate |
| Customers | Create, get by ID, list with pagination, partially update, and logically deactivate |
| Orders | Place an order, get an order by ID, and list orders with pagination |
| Accounts | Domain model and persistence; transactional local Customer/Account registration use case with duplicate checks; authenticated local Account lookup with `GET /accounts/me` |
| Authentication | Google login through Auth0 and authenticated identity lookup with `GET /auth/me` |

Products and customers remain readable after logical deactivation. Both must be
active to participate in a new order. Customer records are business entities;
they are separate from Accounts.

Account persistence stores a normalized email, role, Customer link, external
identity issuer and subject, and database-managed timestamps. The issuer and
subject pair is unique, and both values are preserved without normalization.
Local passwords are no longer stored. `GET /accounts/me` resolves an existing
Account from the authenticated identity. Local registration is implemented by
`RegisterCustomerAccountUseCase`: it saves Customer and a linked CUSTOMER Account
in one transaction and rejects existing identities and emails before saving.
The onboarding endpoint, trusted Auth0 email retrieval, and role/ownership
authorization remain pending. Existing
Customer email updates do not yet synchronize Account email.

The first login version uses Google only through Auth0. Local passwords,
Argon2id, pepper, and account linking are outside that version. Account domain
and persistence store external identities. Spring Security validates Auth0
access tokens, and `/auth/me` returns the authenticated issuer and subject.
There is no frontend yet; the development login flow uses Postman and a browser.
See the [authentication guide](docs/authentication.md).

### Adopted access policy (pending implementation)

Visitors and logged-in users can browse products
without login. Public API reads are `GET /products` and
`GET /products/{id}`; no dedicated comparison endpoint exists. Completing a
purchase will require Google authentication and completed local onboarding.
Onboarding will create Customer first, then Account with its Customer ID, in
one transaction. Account represents access; Customer represents the purchasing
profile.

The adopted onboarding email policy uses the same verified Auth0 email to
initialize both records and collects name and phone. Email must come from a
trusted backend integration, not a frontend verification flag. Later provider
email changes will not automatically synchronize local records. Existing local
email conflicts will return a conflict response without automatic linking;
issuer and subject remain the identity key. The local use case implements
creation and duplicate checks, assuming its email input has already been
verified by a trusted integration. That integration and the onboarding HTTP
contract remain pending; the command does not verify provider email itself.

Each administrator will use an individual ADMIN Account for store management,
without a Customer link or permission to purchase. Personal purchases require
a separate CUSTOMER Account, using a different Google identity under the
current single-role model. Public registration will always assign CUSTOMER;
ADMIN access will be provisioned separately through a controlled process still
to be defined. More granular staff permissions are deferred until needed.

The no-Customer-link rule for ADMIN is implemented in the domain and database
through V8. Public catalog reads are implemented; other routes still require
authentication, and role/ownership enforcement is pending. See the
[access decisions](docs/decisions.md#public-catalog-and-purchase-onboarding-planned).

## Getting started

### Requirements

- Java 21
- Docker with Docker Compose

The Maven Wrapper is included, so a separate Maven installation is not needed.
Docker runs PostgreSQL locally and is also required by the persistence tests.

### Configure and run

1. Copy the environment template and replace its placeholder database values.
   Configure the Auth0 issuer and API audience for your environment as well:

   ```bash
   cp .env.example .env
   ```

   The current development values are:

   ```dotenv
   AUTH0_ISSUER_URI=https://setupshop-dev.us.auth0.com/
   AUTH0_AUDIENCE=https://api.setupshop.example
   ```

   These values are public configuration. Keep the trailing slash on the
   issuer. The audience is the API Identifier in Auth0, not its deployment URL.

2. Start PostgreSQL:

   ```bash
   docker compose up -d
   ```

3. Export the same variables for Spring Boot and start the application:

   ```bash
   set -a
   source .env
   set +a
   ./mvnw spring-boot:run
   ```

The API runs at `http://localhost:8080` by default. Flyway applies migrations
on startup, and Hibernate validates the resulting schema. Keep `.env` local;
it may contain database credentials.
The `.env` file is not loaded automatically by `spring-boot:run`; export its
variables in the same terminal as shown above.

### API documentation

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)

These routes also require authentication under the current security policy.
Opening Swagger UI in an unauthenticated browser is not a configured login
flow. Swagger UI has no configured OAuth2 login or Bearer authorization control
yet; use Postman for authenticated calls.

## Authentication

`GET /products` and `GET /products/{id}` are public and need no token. All other
requests require an Auth0 access token in the HTTP header:

```http
Authorization: Bearer <access_token>
```

The API validates signature, issuer, audience, and token validity times. Use
the access token intended for the SetupShop API; the ID token is for the login
client. The [authentication guide](docs/authentication.md) explains the Auth0
setup and the Postman Authorization Code + PKCE configuration.

For the curl examples below, read a fresh access token into a local shell
variable without putting it directly in a command:

```bash
read -rs -p 'Access token: ' ACCESS_TOKEN
printf '\n'
```

Inspect the authenticated identity:

```bash
curl -i http://localhost:8080/auth/me \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

An illustrative `200 OK` body is:

```json
{
  "issuer": "https://setupshop-dev.us.auth0.com/",
  "subject": "google-oauth2|example-user"
}
```

The same call without a token returns `401 Unauthorized`. This endpoint does
not look up Account or Customer. Apart from the public product reads, the
current policy requires authentication but does not enforce local roles or
Order ownership. Order placement
still accepts `customerId` from the request, and Order reads are not restricted
to the authenticated Customer yet.

## API

### Current Account

```bash
curl -i http://localhost:8080/accounts/me \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

For an existing local Account, the response is `200 OK` with a body like:

```json
{
  "id": 1,
  "email": "customer@example.com",
  "role": "CUSTOMER",
  "customerId": 10
}
```

The lookup uses issuer and subject from the validated JWT, not caller-supplied
IDs or email. ADMIN Accounts have a null Customer ID. Missing authentication
returns 401. A valid identity without a local Account returns 404 with the
standard API error body and message `Account not found for external identity`.
That response does not invalidate the JWT or create local records. Onboarding
remains pending. `/auth/me` continues to expose the JWT identity independently
of local registration. Neither endpoint establishes role or Order ownership
permissions for other operations.

### Endpoints

| Method | Path | Result |
| --- | --- | --- |
| `GET` | `/auth/me` | Return the authenticated issuer and subject (`200`) |
| `GET` | `/accounts/me` | Return the authenticated local Account (`200`); no local Account (`404`); missing authentication (`401`) |
| `POST` | `/products` | Create a product (`201`) |
| `GET` | `/products/{id}` | Get a product (`200`) |
| `GET` | `/products?page=0&size=20` | List products (`200`) |
| `PATCH` | `/products/{id}` | Partially update a product (`200`) |
| `DELETE` | `/products/{id}` | Logically deactivate a product (`204`) |
| `POST` | `/customers` | Create a customer (`201`) |
| `GET` | `/customers/{id}` | Get a customer (`200`) |
| `GET` | `/customers?page=0&size=20` | List customers (`200`) |
| `PATCH` | `/customers/{id}` | Partially update a customer (`200`) |
| `DELETE` | `/customers/{id}` | Logically deactivate a customer (`204`) |
| `POST` | `/orders` | Place and return an order (`201`) |
| `GET` | `/orders/{id}` | Get an order (`200`) |
| `GET` | `/orders?page=0&size=20` | List order summaries (`200`) |

Product, customer, and order lists use zero-based pages. The default page is
`0`, the default size is `20`, and size must be between `1` and `100`. List
responses include `content`, `page`, `size`, `totalElements`, and `totalPages`.

### Products

Create a product with a required name and price. Description is optional:

```http
POST /products
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "name": "Keyboard AULA F75",
  "description": "Wireless mechanical keyboard",
  "price": 300.00
}
```

```bash
curl -i -X POST http://localhost:8080/products \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Keyboard AULA F75","description":"Wireless mechanical keyboard","price":300.00}'
```

An illustrative `201 Created` response is:

```json
{
  "id": 1,
  "name": "Keyboard AULA F75",
  "description": "Wireless mechanical keyboard",
  "price": 300.00,
  "active": true
}
```

New products start active. Names are trimmed and limited to 200 characters;
descriptions are limited to 500 characters. A price must be non-negative and
have at most two decimal places.

Use `GET /products/{id}` to read one product and `GET /products` to browse them.
Both can be called without an Authorization header:

```bash
curl -i 'http://localhost:8080/products?page=0&size=20'
curl -i http://localhost:8080/products/1
```

`PATCH /products/{id}` accepts one or more of `name`, `description`, and `price`;
omitted fields keep their current values. For example, `{"price": 350.00}`
changes only the price. A blank description clears it. `DELETE /products/{id}`
sets `active` to `false` and returns `204 No Content`; the product stays
readable but cannot be added to a new order.

### Customers

Create a customer with a name, email, and an unformatted 11-digit phone number:

```http
POST /customers
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "name": "Matheus Miranda",
  "email": "matheus.miranda@example.com",
  "phone": "61999999999"
}
```

```bash
curl -i -X POST http://localhost:8080/customers \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Matheus Miranda","email":"matheus.miranda@example.com","phone":"61999999999"}'
```

An illustrative `201 Created` response is:

```json
{
  "id": 1,
  "name": "Matheus Miranda",
  "email": "matheus.miranda@example.com",
  "phone": "61999999999",
  "active": true
}
```

New customers start active. Names and emails are trimmed; emails are stored in
lowercase and must be unique. Names are limited to 200 characters and emails to
255 characters. The phone field contains exactly 11 digits (`0`–`9`) with no
spaces or punctuation.

Use `GET /customers/{id}` to read one customer and `GET /customers` to browse
them. `PATCH /customers/{id}` accepts one or more of `name`, `email`, and
`phone`; omitted fields keep their current values. For example,
`{"email": "new.address@example.com"}` changes only the email. Updates validate
all supplied fields before changing the customer. An email already in use
returns `409 Conflict`. `DELETE /customers/{id}` sets `active` to `false` and
returns `204 No Content`; the customer stays readable but cannot place a new
order.

### Orders

Create an active customer and active products first. Then send their IDs and
quantities. Prices are read from the catalog by the server; they are not part of
the request.

```http
POST /orders
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 2, "quantity": 3 }
  ]
}
```

With existing active IDs, the same request can be sent from a terminal:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"customerId":1,"items":[{"productId":1,"quantity":2},{"productId":2,"quantity":3}]}'
```

An illustrative `201 Created` response, assuming catalog prices of `300.00`
and `200.00`, is:

```json
{
  "id": 1,
  "customerId": 1,
  "items": [
    { "productId": 1, "quantity": 2, "unitPrice": 300.00, "subtotal": 600.00 },
    { "productId": 2, "quantity": 3, "unitPrice": 200.00, "subtotal": 600.00 }
  ],
  "status": "CREATED",
  "total": 1200.00
}
```

An order needs at least one item, and every quantity must be at least `1`.
The use case checks that the customer and products exist and are active, captures
each product's unit price, calculates item subtotals and the order total with
`BigDecimal`, and persists the order. The recorded amounts remain unchanged if
catalog prices change later.

Retrieve a placed order by its ID:

```bash
curl -i http://localhost:8080/orders/1 \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

`GET /orders/{id}` returns the recorded order, including its items, captured
unit prices, subtotals, status, and total. It returns `200 OK` when the order
exists and `404 Not Found` when it does not.

List orders in ascending ID order:

```bash
curl -i 'http://localhost:8080/orders?page=0&size=20' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

An illustrative `200 OK` response with one order is:

```json
{
  "content": [
    { "id": 1, "customerId": 1, "status": "CREATED", "total": 1200.00 }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

The paginated response contains summaries with `id`, `customerId`, `status`,
and `total`; it does not include items. Use `GET /orders/{id}` for the items
and their captured prices. Invalid page or size values return `400 Bad Request`.

| Status | When placing an order |
| --- | --- |
| `201` | Order created |
| `400` | Invalid request or domain input |
| `401` | Missing or invalid access token |
| `404` | Customer or product not found |
| `409` | Customer or product inactive |

### Validation and errors

Controller error responses return a timestamp, HTTP status, reason, message, and request path.
Request validation errors also include a `fieldErrors` map. The complete request
and response schemas are available in Swagger UI.
Authentication failures are handled by Spring Security and do not currently
follow that JSON error contract. The authentication tests assert HTTP 401
without requiring an error body.

## Architecture

The project is organized by business feature. `product`, `customer`, and
`order` each separate business rules, orchestration, and framework code.
`account` contains the domain, identity lookup use case, persistence, and
authentication and current Account HTTP endpoints:

```text
src/main/java/br/com/setupshop/
├── product/
├── customer/
├── account/
│   ├── domain/                  # Account, role, exceptions, repository contract
│   ├── application/usecase/     # Retrieve Account by external identity
│   └── infrastructure/
│       ├── persistence/         # JPA repository and adapter
│       └── web/                 # Authentication/Account controllers, DTOs, error handler
├── order/
│   ├── domain/                  # Models, invariants, repository contract
│   ├── application/usecase/     # Commands, order placement, lookup, and listing
│   └── infrastructure/
│       ├── persistence/         # JPA repository and adapter
│       └── web/                 # Controller, DTOs, and HTTP errors
└── shared/                     # Reusable configuration, pagination, HTTP types
```

Domain repository contracts are implemented by persistence adapters. Use cases
coordinate those contracts and domain objects. Controllers translate HTTP
requests into commands and domain results into response DTOs. Pagination types
used outside infrastructure do not depend on Spring Data.

See the [domain model](docs/architecture/domain-model.md) and
[technical decisions](docs/decisions.md) for the current design.

## Data and migrations

PostgreSQL 17 is the persistent store. Flyway owns schema changes; Hibernate
uses `ddl-auto: validate` rather than creating or modifying tables.

| Migration | Purpose |
| --- | --- |
| `V1` | Create products |
| `V2` | Add product timestamp defaults and an update trigger |
| `V3` | Create customers |
| `V4` | Create orders |
| `V5` | Create order items |
| `V6` | Create accounts with unique email and Customer link, foreign key, and timestamps |
| `V7` | Add required issuer and subject with composite uniqueness and remove password hashes |
| `V8` | Reject ADMIN Accounts with a Customer link |

V7 requires `accounts` to be empty when it runs, as confirmed for the current
local database. It does not convert existing password accounts into external
identities. Applied migrations must remain unchanged.

V8 adds `ck_accounts_admin_without_customer` using
`CHECK (role <> 'ADMIN' OR customer_id IS NULL)`. Existing ADMIN records with
a Customer link must be reviewed before applying it to a populated database.
The user confirmed the local accounts table was empty and successfully applied
V8. Preserve V8 now that it has been applied.

Order items store product IDs, quantities, and captured unit prices. Orders
store their calculated totals and start with status `CREATED`. Database defaults
generate creation timestamps.

Each Customer can have at most one Account. Accounts with the `CUSTOMER` role
require a Customer ID. Account creation and update timestamps are managed by
PostgreSQL defaults and an update trigger.

## Tests and CI

Run the full suite with Docker available. Tests do not require loading `.env`:

```bash
./mvnw test
```

To run a focused test:

```bash
./mvnw -Dtest=OrderControllerTest test
```

The suite covers domain rules and use cases with unit tests, HTTP behavior with
MockMvc, and persistence with Testcontainers, PostgreSQL, and Flyway. GitHub
Actions runs `./mvnw test` on pushes and pull requests using Java 21; the CI
runner needs Docker for Testcontainers.
The controller and security test classes use `@TestPropertySource` with fictitious
Auth0 issuer and audience values. No Auth0 environment variables, production
login, or access token are needed by automated tests. The user reported 191
tests passing with both Auth0 variables removed using:

```bash
env -u AUTH0_ISSUER_URI -u AUTH0_AUDIENCE ./mvnw test
```

This verifies local execution without those variables; a GitHub Actions run
has not been verified in this session. Running the application still requires
the real environment configuration described above.

Account tests cover external identity validation, storage and retrieval,
duplicate identities, and different users sharing the same issuer, as well as
email uniqueness and Customer linkage constraints.
The repository provides `findByIssuerAndSubject`, returning `Optional<Account>`.
Integration tests verify that a matching pair returns the saved Account and
that changing either the issuer or subject returns an empty result. The user
reported 194 passing tests after adding this lookup. The 191-test run above
records the earlier verification without Auth0 environment variables.

`GetAccountByExternalIdentityUseCase` uses the repository to return an existing
Account or throw `AccountNotFoundException` with the message
`Account not found for external identity`. Two unit tests cover both outcomes
and the repository arguments. The latest full-suite result reported by the
user at that stage was 196 passing tests. This
use case is now called by AccountController for `/accounts/me`, and
AccountExceptionHandler maps the exception to 404 with ApiErrorResponse.

The ADMIN restriction full-suite result reported by the user was 200 passing tests with
zero failures, errors, or skipped tests. The ADMIN restriction adds two domain
tests and two persistence tests, covering valid creation/storage without a
Customer ID and rejection of a Customer link. The database rejection test uses
JdbcTemplate to bypass constructor validation and exercise the V8 constraint.

The public catalog full-suite result reported by the user was 203 passing tests with
zero failures, errors, or skipped tests. ProductSecurityTest keeps filters
enabled, imports SecurityConfig, and mocks JwtDecoder and the Product use
cases. It verifies public listing and detail (200) and unauthenticated creation
(401), with no create use case invocation. The creation request includes a
valid body and a CSRF token to isolate the authentication requirement.

The Account HTTP slice result reported by the user was 206 passing tests with
zero failures, errors, or skipped tests. AccountControllerTest uses active
security filters, the real SecurityConfig, and mocked decoder/use case to cover
the local Account response (200), missing authentication (401), and missing
local Account (404 with the standard error JSON). It verifies the identity
arguments passed to the use case and no use case call for unauthenticated access.

The latest full-suite result reported by the user is 214 passing tests with
zero failures, errors, or skipped tests. Local registration adds four unit
tests for creation and duplicate identity/Customer email/Account email rejection,
two PostgreSQL tests for Account email existence, and two integration tests
for committed Customer/Account data and rollback when Account construction
fails. The integration tests invoke the Spring-managed use case without an
enclosing test transaction and inspect database state through JdbcTemplate.
Account database write failure and concurrent conflict handling remain untested.
The new Account conflict handlers map to 409; dedicated HTTP coverage is pending.

`AuthenticationControllerTest` keeps security filters active and replaces
`JwtDecoder` with a mock. It covers 401 for missing/rejected tokens and the
200 identity response for a token accepted by the decoder. It does not
independently test the real decoder's signature, issuer, audience, or time
validation. Product, Customer, and Order controller tests use
`@AutoConfigureMockMvc(addFilters = false)` to test their HTTP contracts;
they do not verify security rules. The user also manually verified `/auth/me`
against Auth0 with a fresh token (200) and without authentication (401).

## Documentation and future scope

Swagger UI documents the current HTTP contract. The domain model and technical
decisions record the design behind it. JWT authentication and external identity
persistence, repository lookup by issuer and subject, and the use case for
retrieving an existing Account through `GET /accounts/me` are implemented.
Transactional local Customer/Account creation is implemented. The onboarding
endpoint and trusted provider profile retrieval remain planned. The Order
access policy's role and ownership rules are not yet
enforced; unauthenticated requests are rejected. Cart, inventory workflows,
payment, messaging, and cloud deployment remain deferred. The current Order
API supports placement, lookup by ID, and paginated listing.
Other order operations have not been added yet.
