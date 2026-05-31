# Solver comparison

Comparison targets:

- HiGHS CLI.
- COIN-OR CLP CLI.
- GLPK CLI.
- OR-Tools Java.
- ojAlgo.

G5 implements the CLI adapter lane for HiGHS, CLP, and GLPK. G6 owns Java
library adapter behavior for OR-Tools and ojAlgo.

## Fairness rules

- Use the same canonical model for all solvers.
- Record any format conversion required by an adapter.
- Report unavailable solvers instead of silently omitting them.
- Record all failures.
- Validate solver outputs independently.
- Record versions and machine metadata.
- Do not hide unfavorable results.

## CLI Adapter Baseline

The CLI comparison suite uses the same solver input envelope for every CLI
adapter. Each adapter exports the model to its own work directory, captures a
bounded diagnostic, normalizes status, and reports unavailable binaries as
records instead of skipped runs.

The comparison lane includes Java library adapters for OR-Tools and ojAlgo.
These adapters use the same solver input envelope as the CLI adapters, but they
solve in process and keep third-party dependencies isolated to their own
modules. OR-Tools native-runtime load failures and unavailable optional solver
capabilities are reported as unavailable solver records.

The comparison smoke runs one tiny Tier 1 instance through HiGHS, CLP, GLPK,
OR-Tools, and ojAlgo. It writes Markdown, JSON, and CSV reports under
`examples/solver-comparison-smoke/build/reports/solver-comparison-smoke` for
both direct example-project runs and the root `solverComparisonSmoke` lane. It
passes when all optional solvers are unavailable, provided each unavailable
solver is reported explicitly. If any available solver returns an adapter error
or fails validation, the lane fails after writing reports.
