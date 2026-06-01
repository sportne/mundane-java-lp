# lp-solver-performance

Performance-oriented in-project solver module.

The G9 performance solver follows
`docs/adr/ADR-0007-performance-solver-approach.md`. The first core is a narrow
two-phase revised-simplex-style tableau over primitive arrays. It supports
continuous nonnegative variables, finite variable upper bounds, finite `<=`
rows, finite lower rows through artificial variables, and simple equality rows.

The module must not execute external processes or depend on third-party solver
libraries. Solver state remains primitive-array based and is exposed only
through normalized `lp-solver-spi` results.

Current limitations are intentional: free variables, shifted lower variable
bounds, ranged rows, presolve, broad scaling/equilibration, warm starts, and
numerical robustness claims are out of scope.

## Correctness coverage

The correctness suite validates solver output through `lp-validation`, not by
asserting only adapter statuses. The current supported Tier 1 fixtures are:

- `single-bounded-variable`
- `two-variable-feasible-optimum`
- `unbounded-nonnegative-ray`
- `redundant-row`
- `equality-row`
- `degenerate-optimum`

The Tier 1 fixtures that require free variables, shifted variable lower bounds,
or ranged-row normalization are recorded as deterministic `UNSUPPORTED`
outcomes until those transformations are in scope.

## Profiling baseline

The first profiling baseline is recorded in
`docs/verification/performance-profiling-baseline.md`. It uses
`benchmarkSmoke` output for the generated three-node network-flow fixture and
selects sparse-to-constraint setup allocation as the first optimization target.
The baseline is not a performance claim.

G9-012 keeps the same solver semantics while replacing full dense CSR row
materialization with caller-owned row buffers. The change reduces setup
allocation pressure before tableau construction; it does not change the
remaining tableau copy or make a runtime performance claim.

G9-013 reran the performance solver correctness suite after the sparse hot-path
change and removed stale stub-era comments and repeated test setup. No
correctness fixtures changed status.

G9-014 removed the solve-path constraint-record staging step. Supported
constraints are counted first and then written directly into tableau storage,
which reduces setup copying while preserving the same phase and pivot behavior.

G9-015 adds deterministic numerical stress coverage. Scaling, degeneracy, and
tight-bound generated fixtures validate through `lp-validation`; the
ill-conditioned ranged-row fixture has feasible evidence but remains an
explicit unsupported outcome for this solver.

G9-016 adds narrow row scaling during tableau construction for non-unit row
constraints. When the largest absolute original coefficient exceeds one,
original coefficients and right-hand sides are divided by that coefficient
scale before slack, surplus, or artificial columns are added. The final primal
is checked against the original unscaled model before reporting `OPTIMAL`. This
keeps existing fixture outcomes stable and improves large-coefficient stress
behavior; ranged rows, shifted lower bounds, broad scaling/equilibration, and
broader numerical claims remain out of scope.

G9-017 records the first smoke-level performance evidence report in
`docs/verification/performance-evidence-report.md`. The report compares the
in-project simple and performance solvers with available third-party adapters
only as validation and reporting evidence; it makes no runtime superiority
claim.

See the repository root `README.md`,
`docs/architecture/module-boundaries.md`, and
`docs/architecture/solver-adapter-contract.md`.
