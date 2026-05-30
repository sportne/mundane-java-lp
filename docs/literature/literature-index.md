# Literature index

The literature set records concise design drivers for solver, validation,
sparse-storage, and benchmark work. Notes are not surveys; each topic should
name the implementation tasks that need the evidence.

## Topics

- `revised-simplex.md`
- `interior-point-methods.md`
- `first-order-methods.md`
- `presolve.md`
- `scaling.md`
- `sparse-linear-algebra.md`
- `numerical-stability.md`
- `benchmark-methodology.md`

## Reading questions

- Which algorithmic family is best for first Java implementation?
- Which operations dominate memory bandwidth and allocation pressure?
- Which numerical residuals should the harness record independently?
- Which benchmark families represent XA-like massive sparse LPs?

## Baseline sources

- Revised simplex: hyper-sparsity, basis-update cost, and parallel dual-simplex
  references for G9 design.
- Interior point: primal-dual method and sparse factorization risks for
  performance-solver design.
- First order: PDLP/PDHG as the main reference for massive sparse approximate
  LP solving.
- Presolve and scaling: transformation evidence that validation and reports
  must preserve.
- Sparse linear algebra: storage, ordering, factorization, and allocation
  drivers for Java/GraalVM.
- Numerical stability: tolerance profiles, residuals, and refinement as
  evidence policy.
- Benchmark methodology: performance profiles, public benchmark provenance, and
  reproducible smoke lanes.
