# solver-comparison-smoke

Runs a tiny Tier 1 LP comparison through the harness against the HiGHS, CLP,
and GLPK CLI adapters. Missing solver binaries are reported as unavailable
solver outcomes; the smoke still passes when all three binaries are absent. If
an available solver returns an adapter error or fails validation, reports are
written and the lane exits with a failure.

The Gradle lane writes reports under `build/reports/solver-comparison-smoke`:

- `report.md`
- `report.json`
- `report.csv`
