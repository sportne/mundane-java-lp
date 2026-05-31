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

See the repository root `README.md`,
`docs/architecture/module-boundaries.md`, and
`docs/architecture/solver-adapter-contract.md`.
