# Phase 0 - Concepts, thinking, and interview answers

## 1. What a microservice is (and when NOT to use one)
A microservice is a small application that owns one business capability and its data, and can be built, deployed and
scaled on its own. The price you pay: network calls instead of method calls, no shared database transactions,
and many more things to run and monitor.
Honest answer for interviews: for a product this size a **modular monolith** is often the better business choice.
We use microservices here on purpose, to practise distributed-systems design. Say this out loud - interviewers
respect it more than "microservices are always better".

## 2. How to find service boundaries (the thinking process)
1. Write the business rules in plain language (docs/domain/business-rules.md).
2. Underline the **nouns** (book, listing, order, rental, membership) and the **verbs** (verify, negotiate, pay, return).
3. Group things that **change together** and must be **consistent together**. That group is a bounded context.
4. Ask, for each piece of data: "who is the single owner?" One owner per fact.
5. Ask what happens when a service is down. If the answer is "everything stops", the boundary may be wrong.
Example: "Inventory" (where is the physical copy?) is separate from "Catalog" (what do we advertise?) because a
listing can exist while the copy is in transit, and custody history needs its own strict audit trail.

## 3. Database per service, and what it costs
- No joins across services; no shared transactions.
- So an order **copies** the title and price at purchase time (a snapshot). If the catalog price changes later,
  old orders must not change.
- Multi-service workflows (order -> payment -> payout) use a **saga**: a sequence of local transactions with
  compensating actions if a later step fails (Phase 4).

## 4. Sync vs async communication
- Synchronous REST: simple, but the caller waits and fails when the callee is down. Always set timeouts/retries.
- Asynchronous events: decoupled and resilient, but harder to reason about and debug (eventual consistency).
- We start with REST and add events only where the benefit is clear.

## 5. The infrastructure pieces (built in Phase 1)
- **API Gateway:** single front door - routing, authentication check, rate limiting.
- **Service discovery (Eureka):** services find each other by name, not by hard-coded address.
- **Config server:** configuration kept outside the code; per-environment settings.

## 6. Monorepo vs polyrepo
Monorepo: one repo, one CI, easy refactoring and documentation. Polyrepo: strong team ownership but heavy
overhead. A single developer or a small team should use a monorepo.

## 7. ADR (Architecture Decision Record)
A short file recording *what was decided, why, and the consequences*. Write one for each significant choice.
In an interview it shows you can justify decisions, not just copy tutorials.

## 8. Problems we already know are coming
| Problem | Where | Approach |
|---|---|---|
| Distributed transaction (money moves across services) | Phase 4 | Saga with compensation |
| Duplicate payment callbacks | Phase 4 | Idempotency keys |
| "Save to DB and publish event" can half-fail | Phase 4+ | Transactional outbox |
| Two buyers negotiating the same book | Phase 5 | Reservation with locking / optimistic version |
| Late fines calculated at the right time | Phase 6 | Scheduled job + idempotent fine calculation |
| Who has this book right now? | Phase 2/6 | Append-only custody history |

## 9. Likely interview questions for this phase
1. Why microservices for this project? (see section 1)
2. How did you decide the service boundaries? (section 2)
3. How do services get data they don't own? (section 3)
4. Why Maven? Why a monorepo? (ADR-0001/0002)
5. Why a separate DB user per service? (least privilege, isolation)
6. What is in your definition of done? (CONTRIBUTING.md)
