# Solver adapter contract

A solver adapter takes a canonical LP, solver options, and a work directory. It
returns a normalized result. It must not assume that solver-reported status is
accepted without validation.

## Adapter responsibilities

- Record solver ID and version when available.
- Record command line or invocation mode where applicable.
- Apply timeout and thread options where supported.
- Capture logs in a bounded way.
- Normalize status.
- Return primal/dual values when available.
- Report unsupported features explicitly.

## CLI adapter rule

Only CLI adapter modules may execute external processes.
