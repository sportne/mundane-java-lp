# lp-adapter-ortools-java

Java library adapter module for OR-Tools GLOP.

The adapter owns the OR-Tools dependency and native-runtime loading. Foundation
modules and other adapters must not depend on OR-Tools classes. If the native
runtime cannot be loaded on the current machine, the adapter reports
`UNSUPPORTED` through `SolverRunResult` with a deterministic diagnostic.

The 0.1.0 implementation translates the shared solver input envelope into
continuous variables, linear constraints, objective sense and coefficients, then
returns normalized status, optional objective value, primal values when
available, elapsed seconds, and a bounded message.

See the repository root `README.md`, `docs/architecture/module-boundaries.md`,
and `docs/architecture/solver-adapter-contract.md`.
