# ADR-0001: Microservices in a single monorepo

Status: Accepted

## Context
The product has clearly separate areas (identity, catalog, money, rentals, logistics). The author is also
building this to learn and demonstrate production-style distributed systems.

## Decision
Use microservices, kept in one Git repository (monorepo) with one parent POM. Each service is independently
buildable, testable and deployable.

## Consequences
+ One place for CI, versions and documentation; simple for a small team.
+ Real practice with service boundaries, gateways, sagas and messaging.
- More operational complexity than a monolith. For a product this size, a modular monolith would also be a valid
  choice; microservices are chosen here deliberately for learning and showcase value.
