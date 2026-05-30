# Command-line adapters first

## Status

Accepted for G0 scaffold.

## Context

The project targets a future Java/GraalVM LP implementation while first building
a correctness and benchmark harness.

## Decision

CLI adapters reduce early integration risk and keep native/JNI issues out of G0.

## Consequences

- Implementation tasks must preserve the documented boundary.
- Deviations require a new ADR or amendment.
