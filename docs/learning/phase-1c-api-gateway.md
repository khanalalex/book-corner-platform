# Phase 1c - API gateway

## The problem
With a dozen services, a mobile app or React site would need a dozen addresses, each with its own login check,
CORS rules and rate limits. Internal services would all be exposed to the internet.

## The idea
Put **one door** in front: the API gateway.
`Client -> Gateway (:8080) -> [look up service in Eureka] -> auth-service / catalog-service / ...`

## What the gateway does for us
| Concern | Where it lives |
|---|---|
| Routing: `/api/v1/auth/**` goes to auth-service | route definitions |
| Finding instances | Eureka via `lb://auth-service` |
| Load balancing across instances | Spring Cloud LoadBalancer |
| Correlation ID on every request | `RequestIdWebFilter` |
| Authentication (later) | JWT check at the gateway, Phase 1e |
| Rate limiting, CORS, circuit breakers (later) | gateway filters |

## Why reactive (WebFlux)?
A gateway mostly waits on other services. A non-blocking model handles many concurrent waiting connections with
few threads. Business services stay normal Spring MVC; only the gateway is reactive.

## Decisions worth explaining
- **Explicit routes only.** Auto-generated routes from Eureka would publish every service the moment it registers.
- **Request ID filter is a `WebFilter`,** so it runs for every request, even unmatched ones (404).
- **Client IDs are validated** (8-64 safe characters) before being trusted, to prevent log injection.
- **`optional:` config import** makes local development easy. In production remove it so a missing config server
  stops the gateway from starting (fail fast).
- **No business logic in the gateway.** If it grows rules about orders or rentals, those belong in services.

## Try it (three terminals)
1. Discovery server: `mvn -pl infrastructure/discovery-server spring-boot:run`
2. Config server: `mvn -pl infrastructure/config-server spring-boot:run`
3. Gateway: `mvn -pl infrastructure/api-gateway spring-boot:run`

Then:
- Eureka dashboard (http://localhost:8761) lists **API-GATEWAY**.
- `curl.exe -i http://localhost:8080/does-not-exist` returns 404 and an `X-Request-Id` header.
- `curl.exe -i http://localhost:8080/api/v1/auth/ping` returns 503: the route matched, Eureka was asked for
  `auth-service`, and no such service is registered yet. That 503 is the proof that routing + discovery work.

## Not done yet (on purpose)
- JWT validation, CORS for the frontend, rate limiting, circuit breakers, HTTPS.

## Interview questions
1. What is an API gateway and why use one?
2. Gateway vs load balancer vs reverse proxy?
3. Why is Spring Cloud Gateway reactive?
4. What does `lb://auth-service` mean?
5. Why not auto-expose every service from the registry?
6. Where should authentication happen: gateway, services, or both? (Both: gateway rejects early, services still
   verify the token and check roles. Never trust "it came through the gateway".)
