# lp-adapter-glpk-cli

CLI adapter module for GLPK.

The adapter invokes a `glpsol` executable from `PATH`, exports supported solver
inputs as MPS in the supplied work directory, applies timeout options around the
process, captures bounded logs, normalizes status, and reports a deterministic
unavailable result when the binary is absent.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
