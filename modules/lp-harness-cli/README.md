# lp-harness-cli

Command-line entrypoint module for harness execution.

The CLI is intentionally small and explicit. It wires built-in smoke suites and
explicit adapter lists into `lp-harness-api`; it does not perform classpath
adapter discovery or hide unavailable solvers.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
