# lp-adapter-ojalgo

Java library adapter module for ojAlgo.

The adapter owns the ojAlgo dependency. Foundation modules and other adapters
must not depend on ojAlgo classes. The 0.1.0 adapter uses
`ExpressionsBasedModel` for continuous linear programs and reports unsupported
model features through `SolverRunResult` rather than reinterpreting the model.

The adapter returns normalized status, optional objective value, primal values
when available, elapsed seconds, and a bounded message using the shared solver
SPI result contract.

See the repository root `README.md`, `docs/architecture/module-boundaries.md`,
and `docs/architecture/solver-adapter-contract.md`.
