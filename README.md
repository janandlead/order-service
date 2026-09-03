# Order Service

This is an independent Spring Boot Maven project. Open this directory by itself in IntelliJ IDEA or STS. It has its own `orderdb` database and never connects to `inventorydb`.

## Run

Create the database with `CREATE DATABASE orderdb;`, ensure PostgreSQL accepts user/password `postgres`/`postgres`, then run:

```text
mvn spring-boot:run
```

The service listens on port 8081 and calls `http://localhost:8082` through its OpenFeign client.

## API

```http
POST http://localhost:8081/api/orders
Content-Type: application/json

{"productCode":"P1001","quantity":2,"price":75000}
```

The order is saved only when Inventory Service reports sufficient stock.
