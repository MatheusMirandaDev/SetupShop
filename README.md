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
| Orders | Place an order for one customer and one or more products with quantities; get an order by ID |

Products and customers remain readable after logical deactivation. Both must be
active to participate in a new order. Customer records are business entities;
they are separate from any future login accounts.

## Getting started

### Requirements

- Java 21
- Docker with Docker Compose

The Maven Wrapper is included, so a separate Maven installation is not needed.
Docker runs PostgreSQL locally and is also required by the persistence tests.

### Configure and run

1. Copy the environment template and replace its placeholder database values:

   ```bash
   cp .env.example .env
   ```

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

### API documentation

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)

These links are available while the application is running locally.

## API

### Endpoints

| Method | Path | Result |
| --- | --- | --- |
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

Product and customer lists use zero-based pages. The default page is `0`, the
default size is `20`, and size must be between `1` and `100`. List responses
include `content`, `page`, `size`, `totalElements`, and `totalPages`.

### Products

Create a product with a required name and price. Description is optional:

```http
POST /products
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
`PATCH /products/{id}` accepts one or more of `name`, `description`, and `price`;
omitted fields keep their current values. For example, `{"price": 350.00}`
changes only the price. A blank description clears it. `DELETE /products/{id}`
sets `active` to `false` and returns `204 No Content`; the product stays
readable but cannot be added to a new order.

### Customers

Create a customer with a name, email, and an unformatted 11-digit phone number:

```http
POST /customers
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
curl -i http://localhost:8080/orders/1
```

`GET /orders/{id}` returns the recorded order, including its items, captured
unit prices, subtotals, status, and total. It returns `200 OK` when the order
exists and `404 Not Found` when it does not.

| Status | When placing an order |
| --- | --- |
| `201` | Order created |
| `400` | Invalid request or domain input |
| `404` | Customer or product not found |
| `409` | Customer or product inactive |

### Validation and errors

Errors return a timestamp, HTTP status, reason, message, and request path.
Request validation errors also include a `fieldErrors` map. The complete request
and response schemas are available in Swagger UI.

## Architecture

The project is organized by business feature. `product`, `customer`, and
`order` each separate business rules, orchestration, and framework code:

```text
src/main/java/br/com/setupshop/
├── product/
├── customer/
├── order/
│   ├── domain/                  # Models, invariants, repository contract
│   ├── application/usecase/     # Commands, order placement, and lookup
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

Order items store product IDs, quantities, and captured unit prices. Orders
store their calculated totals and start with status `CREATED`. Database defaults
generate creation timestamps.

## Tests and CI

Run the full suite with Docker available:

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

## Documentation and future scope

Swagger UI documents the current HTTP contract. The domain model and technical
decisions record the design behind it. Cart, inventory workflows,
authentication, payment, messaging, and cloud deployment are deferred. The
current Order API supports placement and lookup by ID. Other order operations
have not been added yet.
