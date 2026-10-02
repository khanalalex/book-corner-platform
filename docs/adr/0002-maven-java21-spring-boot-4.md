# ADR-0002: Maven, Java 21, Spring Boot 4.0.x, Spring Cloud 2025.1.x

Status: Accepted

## Decision
- Build tool: Maven (most common in enterprise Spring teams; strict conventions; parent POM manages versions).
- Java 21 (LTS). Spring Boot 4.0.8 with Spring Cloud 2025.1.3 (the release train that supports Boot 4.0.x).

## Consequences
- Boot and Cloud versions must always be upgraded together; check the Spring Cloud compatibility table first.
- Spring Boot 4 is a new major generation (modularized starters, Jackson 3). Use current docs, not older tutorials.
