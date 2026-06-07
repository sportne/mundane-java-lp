# First-order methods

## Design notes

- First-order LP methods are relevant for massive sparse instances because they
  can trade high-accuracy basis information for matrix-vector throughput and
  lower per-iteration memory pressure.
- PDLP is the main practical reference for this project: it applies PDHG to LP
  and adds engineering features such as presolve, scaling/preconditioning,
  adaptive restart, and adaptive step choices.
- A first-order solver would require the validation layer to treat approximate
  certificates and residual tolerances as first-class outputs. It should not be
  mixed with the simple correctness-first solver.
- The G9 simple solver is deliberately not first-order: its job is to enumerate
  tiny hand-checkable cases and make terminal statuses inspectable, not to scale
  matrix-vector throughput.
- For Java/GraalVM, the implementation question is whether sparse
  matrix-vector kernels and allocation discipline can be made predictable enough
  before adding algorithmic complexity.
- ADR-0007 does not choose a first-order implementation for 0.1.0. PDLP-style
  residual and scaling machinery is outside the release scope; G9 uses a
  restricted revised-simplex-style solver to keep the first performance
  experiment inspectable.

## Roadmap references

- `g3-001-validation-design-completion`
- `g7-004-performance-benchmark-suite-baseline`
- `g9-005-performance-solver-design-decision`
- `g9-016-performance-iteration-3-robustness-and-scaling`

## References

- Applegate et al., "Practical Large-Scale Linear Programming using
  Primal-Dual Hybrid Gradient," NeurIPS 2021,
  <https://research.google/pubs/practical-large-scale-linear-programming-using-primal-dual-hybrid-gradient/>.
- Applegate et al., arXiv version, <https://arxiv.org/abs/2106.04756>.
