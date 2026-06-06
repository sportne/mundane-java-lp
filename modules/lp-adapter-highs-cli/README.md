# lp-adapter-highs-cli

CLI adapter module for HiGHS.

The adapter invokes a `highs` executable from `PATH`, exports supported solver
inputs as MPS in the supplied work directory, applies timeout/thread options when
supported by the CLI, captures bounded logs, normalizes status, and reports a
deterministic unavailable result when the binary is absent.

The 0.1.0 command form is `highs --model_file=<model.mps>
--solution_file=<solution.sol> --options_file=<highs.options>
--time_limit=<seconds>`. The options file carries the requested thread count.
Output parsing is intentionally narrow: status and objective evidence are read
from HiGHS text output, malformed numeric evidence is reported as an adapter
error, and unsupported MPS inputs are reported without starting the external
process.

CI provisions `highs` from the pinned upstream
`v1.14.0/highs-1.14.0-x86_64-linux-gnu-static-mit.tar.gz` archive. Local
machines may use any compatible `highs` on `PATH`; run
`./gradlew verifySolverToolchain --console=plain` from the repository root to
see the discovered path and version diagnostic.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
