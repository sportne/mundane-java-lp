# Simple solver approach

## Status

Accepted for G9 simple solver work.

## Context

The project needs an in-project solver that can exercise the harness,
validation, MPS path, and native-friendly module boundaries without pretending
to compete with mature LP implementations.

Tier 1 correctness fixtures are intentionally tiny and hand-checkable. They are
better served by a transparent solver that makes candidate points and terminal
statuses easy to inspect than by an early general-purpose algorithm with hidden
numerical behavior.

## Decision

Implement a tiny deterministic vertex/enumeration solver for low-dimensional
continuous LPs.

The simple solver optimizes for evidence clarity:

- support only the small Tier 1 shapes needed by the correctness suite;
- use direct one-dimensional interval checks and two-dimensional vertex
  enumeration;
- return deterministic unsupported outcomes outside the approved shape;
- keep numerical tolerance handling explicit and conservative;
- expose results only through the solver SPI.

## Non-goals

- broad LP support;
- competitive runtime;
- revised-simplex basis management;
- dual certificates;
- numerical robustness claims;
- public performance claims.

## Consequences

- The simple solver is useful as a correctness and integration oracle for tiny
  cases, not as a production solver.
- Unsupported outcomes are expected for shapes beyond the Tier 1 subset.
- Later performance-solver work must use a separate design decision and module.
