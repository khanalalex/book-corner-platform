# ADR-0006: API gateway as the single public entry point

Status: Accepted

## Decision
- Spring Cloud Gateway, reactive (WebFlux) flavour, on port 8080. Clients only ever talk to the gateway.
- Routes are declared explicitly. Automatic discovery-locator routes are NOT enabled, so a new service is never
  exposed publicly by accident.
- Routes use `lb://<service-name>`: the gateway looks the service up in Eureka and load-balances across instances.
- Every request gets an `X-Request-Id` correlation ID (generated, or accepted from the client only if it is safe).
- Authentication (JWT validation) is added at the gateway once the auth-service exists (Phase 1e).

## Consequences
+ One place for cross-cutting concerns: security, rate limiting, CORS, request IDs, metrics.
+ Internal services can stay unreachable from outside.
- The gateway is a critical, high-traffic component: it must stay thin (no business logic) and be run in
  multiple instances in production.
