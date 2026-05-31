# lp-adapter-highs-cli

CLI adapter module for HiGHS.

The adapter invokes a `highs` executable from `PATH`, exports supported solver
inputs as MPS in the supplied work directory, applies timeout/thread options when
supported by the CLI, captures bounded logs, normalizes status, and reports a
deterministic unavailable result when the binary is absent.

The 0.1.0 command form is `highs --model_file=<model.mps>
--solution_file=<solution.sol> --write_solution_to_file=true
--time_limit=<seconds> --threads=<n>`. Output parsing is intentionally narrow:
status and objective evidence are read from HiGHS text output, malformed numeric
evidence is reported as an adapter error, and unsupported MPS inputs are
reported without starting the external process.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
