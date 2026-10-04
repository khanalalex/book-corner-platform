# Phase 1b - Centralized configuration

## The problem
Twelve services, each with its own application.yml: change one shared setting (the Eureka URL) and you edit twelve
files, rebuild twelve images, and hope nobody forgot one. Settings also differ per environment (dev, test, prod).

## The idea (twelve-factor "config is separate from code")
Keep configuration outside the application. Each service asks the config server at startup:
"I am `auth-service`, profile `dev`" and receives the merged result of:
1. `application.yml` (shared by all)
2. `auth-service.yml` (this service)
3. profile-specific files such as `auth-service-prod.yml` (most specific wins)

## What we built
- `config-server` on port 8888, reading the `config-repo/` folder (native profile).
- HTTP Basic login so only trusted services can read settings.
- `config-repo/application.yml`: shared Eureka URL and actuator exposure.

## Rules to remember
- **No secrets in config-repo.** Passwords/keys come from environment variables or a secret manager.
  A config repo is readable by many people and lives in Git history forever.
- Config-first vs discovery-first: clients could find the config server through Eureka, but then startup depends
  on Eureka being up first. A known URL is simpler and more robust.
- Production uses a Git backend for versioning and audit trail.

## Trade-off to say out loud in interviews
Central config makes the config server a critical dependency. In production run more than one instance and decide
what a service does when it cannot reach it (fail fast is usually right).

## Try it
- Browser: http://localhost:8888/application/default (login: config / config-dev-pw)
- It returns JSON listing the property source `.../config-repo/application.yml`.
- http://localhost:8888/actuator/health is open (UP).

## Interview questions
1. Why externalize configuration?
2. How do you keep secrets out of a config repository?
3. Config-first vs discovery-first bootstrap?
4. What happens if the config server is down when a service starts?
5. How would you change a setting at runtime without restarting? (Spring Cloud Bus / refresh endpoint, later.)
