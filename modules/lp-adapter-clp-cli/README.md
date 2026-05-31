# lp-adapter-clp-cli

CLI adapter module for COIN-OR CLP.

The adapter invokes a `clp` executable from `PATH`, exports supported solver
inputs as MPS in the supplied work directory, applies timeout options when
supported by the CLI, captures bounded logs, normalizes status, and reports a
deterministic unavailable result when the binary is absent.

The 0.1.0 command form is `clp <model.mps> -seconds <limit> -threads <n> -solve
-solution <solution.sol>`. Output parsing is intentionally narrow: status and
objective evidence are read from CLP text output, malformed numeric evidence is
reported as an adapter error, and unsupported MPS inputs are reported without
starting the external process.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
