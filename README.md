# SetupShop

SetupShop is a REST API for managing products in an e-commerce system.

The project was built with Java and Spring Boot to demonstrate domain modeling,
business rule validation, database migrations, automated testing, and API documentation.

## Features

- Create products
- Find products by ID
- List products with pagination
- Partially update products
- Logically deactivate products
- Validate product domain rules
- Standardized validation and error responses
- Interactive API documentation with Swagger UI

## Technologies

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker Compose
- Maven
- JUnit 5
- Mockito
- Testcontainers
- OpenAPI / Swagger UI

## Architecture

The project is organized by business feature and separates its main responsibilities:

```text
product
├── application
│   └── usecase
├── domain
│   ├── exception
│   ├── model
│   └── repository
└── infrastructure
    ├── persistence
    └── web

shared
├── config
└── pagination
```
- Domain: product model, invariants and repository contract.
- Application: use cases that coordinate business operations.
- Infrastructure: HTTP controllers, persistence adapters and framework integrations.
- Shared: reusable configuration and pagination components.

## API endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/products` | Create a product |
| GET | `/products/{id}` | Find a product by ID |
| GET | `/products?page=0&size=20` | List products with pagination |
| PATCH | `/products/{id}` | Partially update a product |
| DELETE | `/products/{id}` | Logically deactivate a product |

Deactivated products remain available for consultation, while the `active` fie

## Running the project

### Requirements

- Java 21
- Docker
- Docker Compose

### Environment configuration

Copy the example environment file:

```bash
cp .env.example .env
```

Review the values and load them into the current terminal session:

```bash
set -a
source .env
set +a
```

Do not commit the `.env` file because it may contain sensitive credentials.

### Start PostgreSQL

```bash
docker compose up -d
```

### Start the application

```bash
./mvnw spring-boot:run
```

The API will be available at:

[http://localhost:8080](http://localhost:8080)

## API documentation

After starting the application, access Swagger UI:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

The OpenAPI specification is available at:

[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Running the tests

Make sure Docker is running because the persistence integration tests use Testcontainers with a real PostgreSQL instance.

```bash
./mvnw test
```

The test suite includes:

- Domain unit tests
- Application use case tests
- Web layer tests with MockMvc
- Persistence integration tests with PostgreSQL and Flyway

## Database migrations

Flyway applies the database migrations automatically when the application starts.

The migrations create the product table, configure database-generated timestamps, and update `updated_at` when a product is modified.

## Roadmap

- Customer management
- Order management
- Product stock management
- Authentication and authorization
- Order processing workflow