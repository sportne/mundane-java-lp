# Result validation before performance claims

## Status

Accepted for G0 scaffold.

## Context

The project targets a future Java/GraalVM LP implementation while first building
a correctness and benchmark harness.

## Decision

Performance numbers are not meaningful if correctness and tolerance evidence are absent.

## Consequences

- Implementation tasks must preserve the documented boundary.
- Deviations require a new ADR or amendment.
