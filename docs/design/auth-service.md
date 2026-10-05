# Design: auth-service (Phase 1d, for review before coding)

## 1. Responsibility
Owns **identity and access**: accounts, credentials, roles, and issuing tokens.
It does NOT own seller verification (Phase 3), business profiles, or anything about books.

## 2. Actors and roles
| Role | Who | How it is obtained |
|---|---|---|
| USER | everyone who registers | automatically at registration |
| SELLER | verified seller (new books) | added by auth-service when seller-verification-service approves (Phase 3) |
| ADMIN | Book Corner staff | bootstrap admin from environment variables, then created by an admin |
| RIDER | Book Corner delivery rider | created by an admin (riders do not self-register) |
A user can hold several roles (e.g. USER + SELLER).

## 3. Token strategy
| Item | Decision | Why |
|---|---|---|
| Access token | JWT, **RS256** (asymmetric), **15 min** | Services verify with the public key only; no shared secret to leak |
| Claims | `sub` (user UUID), `roles`, `iss`, `aud`, `jti`, `iat`, `exp`; header `kid` | Minimal: no email, no personal data |
| Refresh token | random 256-bit opaque string, **14 days**, stored **hashed** (SHA-256) | A database leak does not leak usable tokens |
| Rotation | every refresh issues a new refresh token and revokes the old one | Limits the value of a stolen token |
| Reuse detection | presenting an already-used refresh token revokes the whole token family | Detects theft |
| Public keys | `GET /.well-known/jwks.json` | Standard; allows key rotation via `kid` |
| Validation | gateway rejects bad tokens early; each service validates again | Defence in depth: never trust "it came through the gateway" |
Trade-off: roles inside a token can be up to 15 minutes stale after a change. Acceptable here; the refresh step picks up changes.

## 4. Data model (`auth_db`, Flyway migrations)
**users**: id (UUID, BINARY(16)), email (unique, stored lowercase), password_hash, full_name, phone (optional),
status (ACTIVE / DISABLED), failed_login_attempts, locked_until, created_at, updated_at, version (optimistic lock).
**roles**: id, name (USER, SELLER, ADMIN, RIDER).
**user_roles**: user_id, role_id (composite key).
**refresh_tokens**: id, user_id, family_id, token_hash (unique), issued_at, expires_at, revoked_at, replaced_by,
user_agent, ip.
Timestamps are UTC. IDs are UUIDs so they cannot be guessed or enumerated and need no cross-service coordination.
Other services store only the user's UUID, never a copy of the account.

## 5. Endpoints (all JSON, versioned)
| Method + path | Access | Purpose |
|---|---|---|
| POST /api/v1/auth/register | public | create account (role USER) |
| POST /api/v1/auth/login | public | returns access + refresh token |
| POST /api/v1/auth/refresh | public (needs refresh token) | rotate tokens |
| POST /api/v1/auth/logout | authenticated | revoke the refresh token family |
| GET /api/v1/users/me | authenticated | own profile |
| PUT /api/v1/users/me | authenticated | update name, phone |
| POST /api/v1/users/me/password | authenticated | change password (revokes all refresh tokens) |
| POST /api/v1/admin/riders | ADMIN | create a rider account |
| PATCH /api/v1/admin/users/{id}/status | ADMIN | enable / disable an account |
| GET /.well-known/jwks.json | public, internal use | public signing keys |
| PUT /internal/users/{id}/roles | service-to-service only | grant SELLER (Phase 3) |
Errors use the standard Problem Details format (RFC 9457). Validation errors list each failing field.

## 6. Security rules
- Passwords: hashed with Spring's delegating encoder (bcrypt now, upgradeable). **Maximum 72 bytes**, because bcrypt
  silently ignores anything longer; minimum 10 characters; never logged, never returned.
- Login failure message is always "Invalid email or password" (no hint whether the email exists).
- Lockout: 5 consecutive failures lock the account for 15 minutes. Gateway rate limiting is added later.
- Email is trimmed and lowercased before storing and before lookup.
- Duplicate email: 409. A concurrent double registration is caught by the unique constraint, not by a pre-check alone.
- Known limitation: a 409 on registration reveals that an email exists. The usual fix is email verification
  with a generic response; planned after notification-service exists.
- Signing keys: a private key supplied by environment/secret, never committed. Dev profile may generate a temporary
  key pair at startup (tokens stop working after restart); other profiles refuse to start without a configured key.
- Clock skew tolerance of 30 seconds when validating expiry.

## 7. Bootstrap admin
On first start, if no ADMIN exists, create one from `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD`
environment variables. Without them, no admin is created.

## 8. Technology
Spring Boot 4, Spring Web MVC, Spring Data JPA (MySQL), Flyway, Spring Security (+ Nimbus JOSE for signing),
Bean Validation, springdoc OpenAPI, Testcontainers MySQL for integration tests.
Exact starter names for Boot 4 are verified when coding (several were renamed in Boot 4).

## 9. Coding order for Phase 1e (one small step at a time)
1. Module skeleton, Flyway migration, entities, repositories (+ Testcontainers test)
2. Registration (validation, hashing, duplicate handling, error format)
3. Login + JWT signing + JWKS endpoint
4. Refresh rotation, reuse detection, logout
5. `/users/me`, change password
6. Admin: bootstrap admin, create rider, disable user
7. Gateway JWT validation + route updates
8. OpenAPI docs and test pass

## 10. Out of scope for now
Email verification, forgot-password, Google login, social login, MFA, rate limiting. Each is a later addition and
the design above does not block any of them.

## 11. Alternatives considered
- **Keycloak / Auth0 / Cognito:** in many companies this is the right production answer. We build our own because
  requirements are simple and the learning value is high. We keep standard tokens (JWT, JWKS) so it could be swapped.
- **Spring Authorization Server (full OAuth2/OIDC):** powerful but heavy; unnecessary for a first-party app.
- **HS256 shared secret:** every service would hold the signing secret, so any one leak lets an attacker mint tokens.
- **Sessions + cookies:** awkward across many services and mobile clients.
