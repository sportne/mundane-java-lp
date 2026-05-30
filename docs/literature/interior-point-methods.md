# Interior-point methods

## Design notes

- Primal-dual interior-point methods are a candidate for the performance solver
  only after the model, sparse matrix, residual, and benchmark layers are stable.
  The implementation risk is dominated by sparse linear algebra and numerical
  safeguards, not by the outer iteration alone.
- The harness must record primal residuals, dual residuals, complementarity,
  objective values, and status translation separately. Interior-point solvers
  can be close to optimal without producing a basic solution, so validation must
  not assume simplex-style basis artifacts.
- A Java implementation should avoid committing to dense normal equations in
  early gates. Factorization strategy, ordering, scaling, and refinement need
  explicit design evidence before any performance claim.
- Native-image friendliness depends on keeping factorization dependencies and
  workspace ownership explicit.

## Roadmap references

- `g3-001-validation-design-completion`
- `g7-003-benchmark-evidence-baseline`
- `g9-005-performance-solver-design-decision`
- `g9-015-numerical-robustness-stress-suite`

## References

- Wright, *Primal-Dual Interior-Point Methods*, SIAM, 1997,
  <https://doi.org/10.1137/1.9781611971453>.
- Applegate et al., "Practical Large-Scale Linear Programming using
  Primal-Dual Hybrid Gradient," NeurIPS 2021,
  <https://research.google/pubs/practical-large-scale-linear-programming-using-primal-dual-hybrid-gradient/>.
