# Phase 1e-1 - Persistence, migrations, and testing against a real database

## 1. Migrations instead of "let Hibernate create the tables"
`ddl-auto: update` lets Hibernate change the schema on its own: fine for a demo, dangerous for production (it can
drop nothing, rename nothing, and gives no history). We use **Flyway**:
- Each change is a numbered SQL file (`V1__...`, `V2__...`) that runs once, in order, and is recorded in the
  `flyway_schema_history` table.
- **Never edit a migration that has been merged.** Flyway checks a checksum and refuses to start. Add a new file.
- Hibernate is set to `ddl-auto: validate`: it only *checks* that entities match the tables and fails at startup
  if they do not. This catches a forgotten migration before it reaches production.

## 2. Design choices in the schema
| Choice | Reason |
|---|---|
| UUID primary keys (`BINARY(16)`) | Not guessable, no sequence to coordinate between services. Cost: random UUIDs spread inserts across the index; time-ordered UUIDs (v7) are a later optimisation |
| `email` unique constraint in the database | The only safe way to stop duplicates under concurrent requests: a "check then insert" can race |
| `@Version` (optimistic locking) | Two simultaneous updates to one user: the second fails instead of silently overwriting |
| Refresh token stored as SHA-256 hash | A database leak does not leak usable tokens |
| `DATETIME(6)` in UTC | One clock for every service and server; no timezone bugs |
| `spring.jpa.open-in-view: false` | Stops the database being used while the response is rendered (hidden lazy loading, held connections) |
| Roles seeded by migration with fixed ids | Every environment has identical reference data |

## 3. Gotchas we avoided (good interview stories)
- **Enums on MySQL:** Hibernate expects a native `ENUM` column for `@Enumerated(STRING)`. Our columns are `VARCHAR`,
  so we add `@JdbcTypeCode(SqlTypes.VARCHAR)`; otherwise schema validation fails.
- **`CHAR` vs `VARCHAR`:** validation is strict about the column type, so the token hash is `VARCHAR(64)`, not `CHAR(64)`.
- **Spring Boot 4 starter changes:** `spring-boot-starter-webmvc` (was `-web`) and `spring-boot-starter-flyway`
  (Flyway no longer auto-configures from `flyway-core` alone).

## 4. Why Testcontainers, not an in-memory H2 database
H2 pretends to be MySQL. Differences in SQL, types, and constraint behaviour mean tests can pass on H2 and fail in
production. Testcontainers starts a **real MySQL 8.4 in Docker** for the test run, runs the real migrations, and
throws it away. Slower by a few seconds, far more trustworthy. Needs Docker running; GitHub Actions runners have it.

## 5. What the tests prove
1. The migrations run on a real MySQL and Hibernate's validation accepts the schema.
2. The four roles exist after migration.
3. A user can be saved with a role and found by email; emails are normalized to lowercase.
4. The database itself rejects a duplicate email.
5. A refresh token can be stored and found by its hash.

## 6. Interview questions
1. Why Flyway over `ddl-auto: update`? What happens if you edit an applied migration?
2. Why a unique constraint instead of checking for an existing email in code?
3. What is optimistic locking and when does it fail a request?
4. Why store a hash of the refresh token?
5. Why Testcontainers instead of H2?
6. What does `open-in-view: false` change?
