# Solver comparison

Initial comparison targets:

- HiGHS CLI.
- COIN-OR CLP CLI.
- GLPK CLI.
- OR-Tools Java.
- ojAlgo.

G5 owns real CLI adapter behavior for HiGHS, CLP, and GLPK. G6 owns real Java
library adapter behavior.

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

The comparison smoke passes when all optional external binaries are unavailable,
provided each unavailable solver is reported explicitly.
