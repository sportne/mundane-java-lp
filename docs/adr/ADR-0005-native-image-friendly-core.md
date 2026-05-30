# Native Image friendly core

## Status

Accepted for G0 scaffold.

## Context

The project targets a future Java/GraalVM LP implementation while first building
a correctness and benchmark harness.

## Decision

Core modules avoid dynamic runtime features to remain friendly to closed-world compilation.

## Consequences

- Implementation tasks must preserve the documented boundary.
- Deviations require a new ADR or amendment.
