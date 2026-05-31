# lp-harness-cli

Command-line entrypoint module for harness execution.

The CLI is intentionally small and explicit. It wires built-in smoke suites and
explicit adapter lists into `lp-harness-api`; it does not perform classpath
adapter discovery or hide unavailable solvers.

The default command runs a built-in tiny smoke suite through an explicit dummy
adapter so local CLI wiring is testable without external solver binaries.
The external solver comparison smoke is implemented in
`examples/solver-comparison-smoke`, which wires the CLI solver adapters and
report writers through the same harness API.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
