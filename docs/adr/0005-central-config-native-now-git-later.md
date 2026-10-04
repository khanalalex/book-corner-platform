# ADR-0005: Central configuration with Spring Cloud Config

Status: Accepted

## Decision
- All services read shared settings from a config server. Config-first: clients know the config server URL
  directly (it does not register in Eureka), so startup does not depend on discovery.
- Local development: the `native` profile reads the `config-repo/` folder in this repository.
- Production: switch to a Git-backed config repository (versioned, audited, reviewable).
- The config server requires HTTP Basic credentials, supplied by environment variables.
- Secrets never live in `config-repo/`. They come from environment variables or a secret manager.

## Consequences
+ One place for shared settings; per-service overrides; change settings without rebuilding images.
- The config server becomes a critical dependency: it must be highly available in production, and services
  should cache or fail fast with a clear error when it is unreachable.
