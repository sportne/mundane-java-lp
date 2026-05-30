# Design-first process

## Status

Accepted for G0 scaffold.

## Context

The project targets a future Java/GraalVM LP implementation while first building
a correctness and benchmark harness.

## Decision

The project starts with docs, gates, and boundaries before solver code.

## Consequences

- Implementation tasks must preserve the documented boundary.
- Deviations require a new ADR or amendment.
