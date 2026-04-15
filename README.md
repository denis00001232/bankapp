# Bank Wallet Application

A Spring Boot application for managing wallet operations (deposit and withdraw) with REST API.

## Features

- REST API for wallet operations (DEPOSIT/WITHDRAW)
- Wallet balance retrieval
- Concurrency handling with pessimistic locking (supports 1000 RPS)
- Database migrations with Liquibase
- Docker containerization
- Configurable via environment variables
- Comprehensive test coverage

## Tech Stack

- Java 17
- Spring Boot 3.5.13
- PostgreSQL 16
- Liquibase
- Docker & Docker Compose
- JUnit 5 with Testcontainers

## API Documentation

**Swagger/OpenAPI documentation is available at:** `http://localhost:8080/openapi.yaml`

This file contains complete API documentation including:
- All endpoints with HTTP methods
- Request/response schemas
- Example requests and responses
- All possible response codes

**Note:** Please refer to the Swagger documentation for detailed API specifications and examples.

## API Endpoints

### Perform Wallet Operation
**POST** `/api/v1/wallet`

Request body:
```json
{
  "walletId": "uuid-here",
  "operationType": "DEPOSIT" | "WITHDRAW",
  "amount": 1000.00
}
```

Response:
- `200 OK` - Operation successful
- `400 Bad Request` - Invalid request (insufficient funds, invalid JSON, validation errors)
- `404 Not Found` - Wallet not found

### Get Wallet Balance
**GET** `/api/v1/wallets/{walletId}`

Response:
```json
{
  "walletId": "uuid-here",
  "balance": 1500.00
}
```

Response codes:
- `200 OK` - Balance retrieved
- `404 Not Found` - Wallet not found
- `400 Bad Request` - Invalid UUID format

## Running with Docker Compose

1. Clone the repository
2. Create `.env` file (optional):
```env
POSTGRES_DB=bankdb
POSTGRES_USER=postgres
POSTGRES_PASSWORD=1234
POSTGRES_PORT=5432
APP_PORT=8080
```

3. Start the application:
```bash
docker-compose up --build
```

The application will be available at `http://localhost:8080`

## Configuration

The application can be configured via environment variables:

- `DB_URL` - Database JDBC URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `SERVER_PORT` - Application port (default: 8080)
- `SHOW_SQL` - Show SQL logs (default: true)
- `LOG_LEVEL` - Application log level (default: DEBUG)
- `SQL_LOG_LEVEL` - SQL log level (default: DEBUG)
- `LIQUIBASE_ENABLED` - Enable/disable Liquibase (default: true)

## Running Tests

**Requirements:**
- Docker must be installed and running on your machine

Run tests with Maven:
```bash
./mvnw test
```

Tests use Testcontainers to spin up a PostgreSQL database automatically.

## Database Schema

The `wallets` table is created via Liquibase migration:
- `id` (UUID, primary key)
- `balance` (DECIMAL(15,2), NOT NULL, CHECK >= 0)

## Concurrency Handling

The application uses pessimistic locking (`SELECT ... FOR UPDATE`) to handle concurrent operations on the same wallet. This ensures that:
- No request fails with 50X errors under high load
- Balance updates are atomic and consistent
- Supports 1000+ RPS on a single wallet

## Error Handling

The application handles various error scenarios:
- Wallet not found (404)
- Insufficient funds (400)
- Invalid request format (400)
- Invalid JSON (400)
- Invalid UUID (400)
- Internal server errors (500)
