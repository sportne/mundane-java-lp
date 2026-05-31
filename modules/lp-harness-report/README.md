# lp-harness-report

Report output module for harness JSON, CSV, and Markdown artifacts.

`lp-harness-report` renders existing run records. It is deterministic and has no
responsibility for executing solvers, validating results, or discovering machine
state beyond metadata already captured in the records.

Supported 0.1.0 renderers are Markdown, CSV, and JSON. Each renderer includes
solver identity/status/objective, validation result, solver options, timing
fields, peak-memory measurement state, failure diagnostics, and machine
metadata.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
