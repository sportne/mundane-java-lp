# lp-adapter-glpk-cli

CLI adapter module for GLPK.

The adapter invokes a `glpsol` executable from `PATH`, exports supported solver
inputs as MPS in the supplied work directory, applies timeout options around the
process, captures bounded logs, normalizes status, and reports a deterministic
unavailable result when the binary is absent.

The 0.1.0 command form is `glpsol --freemps <model.mps> --tmlim <seconds>
--write <solution.sol>`. Output parsing is intentionally narrow: status and
objective evidence are read from GLPK text output, malformed numeric evidence is
reported as an adapter error, and unsupported MPS inputs are reported without
starting the external process.

CI provisions `glpsol` from the Ubuntu 24.04 `glpk-utils` package. Local
machines may use any compatible `glpsol` on `PATH`; run
`./gradlew verifySolverToolchain --console=plain` from the repository root to
see the discovered path and version diagnostic.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
