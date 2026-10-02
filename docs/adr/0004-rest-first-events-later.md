# ADR-0004: Synchronous REST first, asynchronous events later

Status: Accepted

## Decision
Services call each other over REST (with timeouts and retries). A message broker is introduced only when a
workflow needs it (payment and delivery events, notifications).

## Consequences
+ Easier to build, debug and learn.
- Temporary coupling between services; mitigated with Resilience4j and later replaced by events where it hurts.
