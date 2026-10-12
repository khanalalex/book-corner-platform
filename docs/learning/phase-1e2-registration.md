# Phase 1e-2 - Registration: validation, password hashing, error format, default-deny security

## 1. The request flow
`POST /api/v1/auth/register` -> Spring Security (is this path public?) -> controller (`@Valid` checks the JSON)
-> `RegistrationService` (one transaction) -> database -> `UserResponse` back to the client.
Controller = HTTP only. Service = rules. Repository = SQL. Mixing these is the most common beginner mistake.

## 2. DTOs, not entities
The API never returns the `User` entity. `UserResponse` has no password hash and no internal fields. An entity
returned directly eventually leaks a field someone adds later, and lazy collections break outside a transaction.
The service builds the DTO inside the transaction.

## 3. Validation
Bean Validation annotations on `RegisterRequest` reject bad input before any business code runs.
- Password rule is **length (min 10), not "needs a symbol"**: length is what actually makes passwords hard to guess.
- **Maximum 72 BYTES.** bcrypt silently ignores everything after byte 72, so two long passwords that differ only
  at the end would be treated as the same. We count bytes in UTF-8 because one character can be several bytes
  (a custom `@MaxUtf8Bytes` constraint).
- Validation errors list the field and a message, never the rejected value (it might be a password).

## 4. Password storage
- Hash with **bcrypt, cost 12**. Hashing is deliberately slow so guessing is expensive. Never encrypt, never store plain text.
- The stored value is `{bcrypt}$2a$12$...`. The prefix names the algorithm, so a stronger algorithm can be added
  later and old hashes keep working (`DelegatingPasswordEncoder`).
- `RegisterRequest.toString()` masks the password so it can never be logged by accident.

## 5. Duplicate emails (two layers, on purpose)
1. `existsByEmail` gives a fast, friendly answer in the normal case.
2. The database **unique constraint** is the only race-proof guarantee: two simultaneous requests can both pass
   step 1. The loser gets a `DataIntegrityViolationException`, which we turn into the same 409.
A unit test simulates that race with a mock, because it is hard to reproduce over HTTP.

## 6. Error format: Problem Details (RFC 9457)
Every error is `application/problem+json` with `title`, `status`, `detail`, plus our stable `code`
(`VALIDATION_FAILED`, `EMAIL_ALREADY_REGISTERED`) and, for validation, an `errors` list. The frontend switches on
`code`, never on English text. One `@RestControllerAdvice` produces all of them.

## 7. Security configuration
- **Default deny:** everything requires authentication except the explicitly public paths.
- **Stateless, CSRF off:** CSRF defends cookie-based sessions; this API uses bearer tokens, not cookies.
- Anonymous requests to protected paths get **401**. A test proves it.
- Spring Boot prints "Using generated security password" at startup. It is harmless noise from Boot's default user
  and disappears in the next step, when we add our own user lookup for login.

## 8. Known limitation (said honestly)
A 409 on registration reveals that an email is registered (user enumeration). The standard fix is email
verification with a generic response, planned with the notification service (design doc section 6b).
Gateway rate limiting will make mass probing expensive.

## 9. The test pyramid in this step
| Test | Speed | Proves |
|---|---|---|
| `RegistrationServiceTest` (Mockito, no Spring) | milliseconds | race branch, missing-role branch |
| `RegistrationApiTest` (MockMvc + real MySQL) | seconds | security, validation, JSON, error format, database |
| `AuthPersistenceTest` | seconds | schema and constraints |
All integration tests share one Spring context and one MySQL container through the `@IntegrationTest` annotation.

## 10. Interview questions
1. Why return a DTO instead of the entity?
2. Why hash passwords, and why bcrypt? What is its 72-byte limit and how do you handle it?
3. Why both an `existsByEmail` check and a unique constraint?
4. What is Problem Details and why add a `code` field?
5. What does "default deny" mean in a security configuration?
6. Why is CSRF protection disabled here? When would you need it?
7. What is user enumeration and how would you prevent it?
