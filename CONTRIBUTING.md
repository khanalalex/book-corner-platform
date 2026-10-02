# Working agreement

## Branches
- `main` is always buildable. Work happens on short branches: `feature/<phase>-<topic>`, e.g. `feature/p1-auth-service`.
- Merge through a pull request, even when working alone: it shows the history and runs CI.

## Commits (Conventional Commits)
`feat: add JWT login endpoint`, `fix: reject expired refresh tokens`, `docs: add ADR for database per service`,
`chore: bump Spring Cloud`, `test: add Testcontainers test for user repository`.
Small commits, one idea each.

## Phases
Each phase ends with a pull request, a merge to `main`, and a tag: `phase-0`, `phase-1`, ...

## Definition of done (every feature)
- Code compiles, tests pass, CI green
- Flyway migration for any schema change
- API documented (OpenAPI)
- Business rule IDs referenced in tests or commit messages
- README / docs updated
