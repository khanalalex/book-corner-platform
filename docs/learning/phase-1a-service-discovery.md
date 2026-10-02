# Phase 1a - Service discovery (Eureka)

## The problem
Service A needs to call Service B. Where is B? In a real system B runs as several instances, on addresses that
change (restarts, scaling, containers get new IPs). Hard-coding `http://localhost:8082` breaks immediately.

## The solution: a service registry
1. Every service **registers** itself at startup: "I am `catalog-service` at 10.0.0.5:8082".
2. It sends a **heartbeat** every 30 seconds (default).
3. If heartbeats stop for 90 seconds (default), the registry **evicts** the instance.
4. A caller asks the registry for "catalog-service" and gets the live instances, then picks one
   (**client-side load balancing**).
In code you write `http://catalog-service/...` and never an IP address.

## Why Eureka, and the trade-off
Eureka favours **availability over consistency** (AP in CAP terms). During a network split it keeps serving its
last known list, which may be slightly stale, instead of refusing to answer. Clients also cache the registry,
so a short registry outage does not stop service-to-service calls.

## Self-preservation mode
If many heartbeats suddenly vanish, Eureka assumes the *network* is broken, not the services, and stops evicting
instances. Good in production, confusing in dev, so we disable it only in local config.

## Settings explained
| Setting | Why |
|---|---|
| `register-with-eureka: false` | The registry should not register with itself |
| `fetch-registry: false` | It has no other registry to copy |
| `server.port: 8761` | Conventional Eureka port |
| actuator `health` | Lets Docker/Kubernetes ask "are you alive?" |

## Not done yet (on purpose)
- The dashboard is open. Before any real deployment it gets basic authentication and runs as more than one instance.

## Interview questions
1. What problem does service discovery solve?
2. Client-side vs server-side load balancing?
3. What happens if the registry goes down? (clients use their cached copy)
4. What is self-preservation mode and when would you disable it?
5. Eureka vs Consul vs Kubernetes DNS? (Kubernetes gives discovery natively, so Eureka matters mostly outside it.)
