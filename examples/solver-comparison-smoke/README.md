# solver-comparison-smoke

Runs a tiny Tier 1 LP comparison through the harness against the HiGHS, CLP,
GLPK, OR-Tools, ojAlgo, simple, and performance adapters. Missing solver
binaries or unavailable optional runtimes are reported as unavailable solver
outcomes; the smoke still passes when optional third-party solvers are
unavailable and in-project solvers validate. If an available solver returns an
adapter error or fails validation, reports are written and the lane exits with a
failure.

The Gradle lane writes reports under `build/reports/solver-comparison-smoke`:

- `report.md`
- `report.json`
- `report.csv`

The root `strictSolverComparison` lane runs the expanded correctness suite. It
requires every baseline solver to be available, covers all Tier 1 canonical
fixtures plus supported MPS round-trip fixtures, and fails on adapter errors or
validation failures after writing reports under
`build/reports/strict-solver-comparison`.
