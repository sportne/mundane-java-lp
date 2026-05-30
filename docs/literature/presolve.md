# Presolve

## Design notes

- Presolve is part of solver behavior, not only input cleanup. Every
  transformation that removes rows, tightens bounds, fixes variables, or detects
  infeasibility must be reversible enough for validation and diagnostics.
- G0-G4 should keep presolve out of the core model. The first implementation
  need is a documented place to record transformations and solver-side messages
  so third-party adapters can be compared consistently.
- The simple solver can initially run without presolve if fixtures are small and
  explicit. The performance solver design must decide which reductions are worth
  implementing and how to test reconstruction.
- Presolve can change benchmark interpretation. Reports should distinguish
  original size, presolved size, solve status, and postsolve validation status.

## Roadmap references

- `g1-001-lp-model-design-completion`
- `g3-001-validation-design-completion`
- `g4-002-harness-execution-model`
- `g9-005-performance-solver-design-decision`

## References

- Andersen and Andersen, "Presolving in Linear Programming," Mathematical
  Programming, 1995, <https://doi.org/10.1007/BF01586000>.
- Applegate et al., "Practical Large-Scale Linear Programming using
  Primal-Dual Hybrid Gradient," NeurIPS 2021,
  <https://research.google/pubs/practical-large-scale-linear-programming-using-primal-dual-hybrid-gradient/>.
