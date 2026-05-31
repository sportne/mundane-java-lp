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
bounds, ranged rows, presolve, scaling, warm starts, and numerical robustness
claims are out of scope.

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

See the repository root `README.md`,
`docs/architecture/module-boundaries.md`, and
`docs/architecture/solver-adapter-contract.md`.
