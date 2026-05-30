# Solver comparison

Initial comparison targets:

- HiGHS CLI.
- COIN-OR CLP CLI.
- GLPK CLI.
- OR-Tools Java adapter placeholder.
- ojAlgo adapter placeholder.

## Fairness rules

- Use the same canonical model for all solvers.
- Record any format conversion required by an adapter.
- Report unavailable solvers instead of silently omitting them.
- Record all failures.
- Validate solver outputs independently.
- Record versions and machine metadata.
- Do not hide unfavorable results.
