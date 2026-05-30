# MPS as initial interchange format

## Status

Accepted for G0 scaffold.

## Context

The project targets a future Java/GraalVM LP implementation while first building
a correctness and benchmark harness.

## Decision

MPS is the first test interchange format because it is common across LP solvers.

## Consequences

- Implementation tasks must preserve the documented boundary.
- Deviations require a new ADR or amendment.
