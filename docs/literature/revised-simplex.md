# Revised simplex

## Design notes

- Revised simplex remains the most relevant baseline for a simple exact-ish LP
  solver because it exposes basis status, pivots, reduced costs, primal and dual
  residuals, and warm-start-like state in a way the harness can validate.
- Modern sparse revised-simplex implementations spend much of their time in
  basis factorization updates, forward solves, backward solves, pricing, and
  ratio tests. The in-project simple solver should not copy these optimizations;
  it should keep the state transparent enough to debug against third-party
  solvers.
- G9 separates the simple correctness-first solver from revised-simplex work.
  The simple solver uses low-dimensional enumeration for Tier 1 evidence
  clarity; revised-simplex concepts move to the later performance-solver
  decision.
- Hyper-sparse behavior matters for the later performance solver. The sparse
  matrix layer should preserve row/column access patterns and avoid hidden dense
  expansion so G9 can experiment with sparse pivot paths.
- Parallel revised simplex is not a G9 starting point. It is useful as a
  reference for what to measure, especially solve counts, vector density, and
  basis-update time.

## Roadmap references

- `g9-001-simple-solver-design-decision`
- `g9-005-performance-solver-design-decision`
- `g9-012-performance-iteration-1-sparse-hot-path`
- `g9-017-performance-evidence-report`

## References

- Hall and McKinnon, "Hyper-sparsity in the revised simplex method and how to
  exploit it," Computational Optimization and Applications, 2005,
  <https://doi.org/10.1007/s10589-005-4802-0>.
- Huangfu and Hall, "Parallelizing the dual revised simplex method,"
  Mathematical Programming Computation, 2018,
  <https://doi.org/10.1007/s12532-017-0130-5>.
- HiGHS cites Huangfu and Hall as the academic reference for its LP revised
  simplex background; use the paper above rather than the project homepage for
  design evidence.
