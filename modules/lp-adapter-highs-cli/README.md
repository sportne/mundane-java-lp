# lp-adapter-highs-cli

CLI adapter module for HiGHS.

The adapter invokes a `highs` executable from `PATH`, exports supported solver
inputs as MPS in the supplied work directory, applies timeout/thread options when
supported by the CLI, captures bounded logs, normalizes status, and reports a
deterministic unavailable result when the binary is absent.

The initial implementation parses HiGHS stdout/stderr for normalized status and
objective evidence. If HiGHS reports an optimal status without a parseable
objective, the adapter returns `ERROR` so validation does not accept incomplete
evidence.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
