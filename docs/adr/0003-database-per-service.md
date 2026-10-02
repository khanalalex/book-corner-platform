# ADR-0003: Database per service

Status: Accepted

## Decision
Each service has its own schema and its own DB user. Services never query each other's tables.
Schema changes are versioned with Flyway.

## Consequences
+ Services can change their schema independently; failures are isolated.
- No cross-service joins or transactions. We copy the data we need (e.g. an order stores the book title and price
  at purchase time) and use sagas for multi-service workflows.
