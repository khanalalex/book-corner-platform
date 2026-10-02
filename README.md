# Book Corner Platform

Online marketplace for **new books, second-hand books and book rentals**, built as Spring Boot microservices.

- Verified sellers sell new books; any user can sell second-hand or rent out their books.
- Buyers and sellers negotiate second-hand prices; Book Corner approves and records the deal.
- Membership unlocks renting Book Corner's own books; late and damage fines are tracked.
- Book Corner handles pickup, store drop-off and delivery with its own riders.

Business rules: [docs/domain/business-rules.md](docs/domain/business-rules.md)
Service map: [docs/architecture/services.md](docs/architecture/services.md)
Decisions: [docs/adr](docs/adr)

## Tech stack
Java 21 - Spring Boot 4.0.x - Spring Cloud 2025.1.x - Maven - MySQL 8.4 + Flyway - Docker Compose - GitHub Actions

## Roadmap
| Phase | Scope | Status |
|---|---|---|
| 0 | Design, repo, parent POM, local MySQL, CI | done |
| 1 | Discovery, config, gateway, auth-service | next |
| 2 | Catalog + inventory | |
| 3 | Seller verification | |
| 4 | Orders, payments, payouts | |
| 5 | Negotiation | |
| 6 | Membership + rentals | |
| 7 | Delivery, reviews, notifications | |
| 8 | Hardening: events, tracing, deployment | |

## Run locally
```bash
cp .env.example .env          # then edit the password
docker compose up -d          # MySQL on localhost:3307
docker compose ps             # wait until mysql is "healthy"
```
Requirements: JDK 21, Maven 3.9+, Docker Desktop.
