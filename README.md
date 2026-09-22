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

## Resilience4j retry and circuit breaker

The inventory call is protected by a Resilience4j retry and circuit breaker named
`inventory-service`.

- A failed inventory-service call is attempted up to 3 times in total (the initial
  call plus 2 retries).
- Retries use a fixed 10-second delay; exponential backoff is disabled.
- Only `InventoryServiceUnavailableException` triggers retries and counts as a
  circuit-breaker failure.
- Business failures such as a missing product or insufficient stock are not
  retried and do not affect the circuit breaker.
- The circuit breaker evaluates the last 10 calls after at least 5 calls have
  completed. It opens when at least 50% have failed.
- An open circuit waits 10 seconds before allowing one test call in the
  `HALF_OPEN` state. A successful test call closes the circuit; a failed one
  opens it again.
- If all attempts fail, the API returns `503 Service Unavailable` and does not
  save an order.

The settings are in `src/main/resources/application.properties`. Runtime logs
show every inventory check, scheduled retry, retry result, rejected call,
fallback, and circuit-breaker state transition.

## Test retry behavior

1. Start PostgreSQL and Order Service, but leave Inventory Service stopped:

   ```text
   mvn spring-boot:run
   ```

2. From another terminal, submit an order:

   ```powershell
   curl.exe -i -X POST http://localhost:8081/api/orders `
     -H "Content-Type: application/json" `
     -d '{"productCode":"P1001","quantity":2,"price":75000}'
   ```

3. Watch the Order Service console. The logs should include entries similar to:

   ```text
   Checking inventory for productCode=P1001 quantity=2
   Retrying inventory-service: attempt=1 wait=10000ms cause=...
   Retrying inventory-service: attempt=2 wait=10000ms cause=...
   Retry inventory-service exhausted after 2 retry attempt(s): ...
   ```

   The request takes about 20 seconds before returning `503`, because there are
   two 10-second waits between the three attempts.

## Test circuit-breaker and recovery behavior

1. Keep Inventory Service stopped and repeat the order request until the failure
   threshold is reached. Look for this state transition:

   ```text
   Circuit breaker inventory-service changed state from CLOSED to OPEN
   ```

2. Calls made while the breaker is open produce a rejection log:

   ```text
   Circuit breaker inventory-service rejected the inventory call because it is OPEN
   ```

3. Start Inventory Service on port `8082`, wait at least 10 seconds, and send the
   request again. The permitted test call should succeed, and the logs should show
   transitions through `HALF_OPEN` back to `CLOSED`. The order response should be
   `201 Created` when stock is available.

To verify the project still compiles and run all automated tests:

```text
mvn test
```
