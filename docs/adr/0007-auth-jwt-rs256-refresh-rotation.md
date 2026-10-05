# ADR-0007: Own auth-service issuing RS256 JWTs with rotating refresh tokens

Status: Proposed (accepted after review)

## Decision
auth-service issues short-lived (15 min) RS256-signed JWT access tokens and long-lived (14 days) opaque refresh
tokens stored hashed, rotated on every use with reuse detection. Services validate tokens using the public keys from
a JWKS endpoint. Details in docs/design/auth-service.md.

## Consequences
+ No shared signing secret; services can verify offline; keys can be rotated using `kid`.
+ Stolen refresh tokens are detected through reuse.
- Access tokens cannot be revoked before expiry (hence the short lifetime).
- We own security-critical code. Mitigation: standard libraries only, thorough tests, review before launch.
