# Sparse linear algebra

## Design notes

- Sparse linear algebra is the shared performance boundary for simplex,
  interior-point, and first-order approaches. The project should keep sparse
  data structures simple, explicit, and benchmarkable before adding solver
  logic.
- CSR and CSC are both useful: matrix-vector products, row activity checks, and
  column-oriented pricing push in different directions. Builders and conversion
  rules need deterministic ordering and duplicate handling.
- Factorization-oriented algorithms depend on symbolic structure, fill control,
  and update behavior. Those are G9 design concerns, not G2 storage concerns.
- Java/GraalVM implications are direct: primitive arrays, predictable ownership,
  and avoided allocation are easier to inspect and profile than generic graph or
  collection-heavy representations.

## Roadmap references

- `g2-001-sparse-storage-design-completion`
- `g2-002-sparse-matrix-test-coverage`
- `g9-010-performance-measurement-instrumentation`
- `g9-012-performance-iteration-1-sparse-hot-path`

## References

- Davis, *Direct Methods for Sparse Linear Systems*, SIAM, 2006,
  <https://doi.org/10.1137/1.9780898718881>.
- Hall and McKinnon, "Hyper-sparsity in the revised simplex method and how to
  exploit it," <https://doi.org/10.1007/s10589-005-4802-0>.
