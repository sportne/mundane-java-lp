# lp-sparse

Primitive sparse matrix storage and kernel module.

`lp-sparse` provides the 0.1.0 sparse matrix representation for LP
coefficients:

- CSR: row-major primitive arrays for row activity kernels.
- CSC: column-major primitive arrays for column-oriented algorithms.
- Small dense-vector multiplication kernels for `y = A x`.
- Low-allocation CSR `multiplyInto(x, y)` and `copyRowInto(row, output)` for
  caller-owned output buffers.

Constructors validate dimensions, pointer arrays, and index ranges. Array input
is copied on construction, and public array accessors must return defensive
copies for values, indices, and pointers. Builders may use temporary objects,
but published matrix instances must materialize primitive arrays before
validation or solver execution.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
