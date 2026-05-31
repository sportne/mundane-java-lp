# lp-adapter-clp-cli

CLI adapter module for COIN-OR CLP.

The adapter invokes a `clp` executable from `PATH`, exports supported solver
inputs as MPS in the supplied work directory, applies timeout options when
supported by the CLI, captures bounded logs, normalizes status, and reports a
deterministic unavailable result when the binary is absent.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
