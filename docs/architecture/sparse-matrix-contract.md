# Sparse matrix contract

Sparse matrix code must use explicit primitive storage. The 0.1.0 sparse layer
is a small storage and kernel layer, not a general linear algebra framework.

## Storage formats

- CSC stores nonzeros column-by-column as `double[] values`, `int[] rowIndices`,
  and `int[] columnPointers`.
- CSR stores nonzeros row-by-row as `double[] values`, `int[] columnIndices`,
  and `int[] rowPointers`.

Both formats use zero-based indices and row-major problem orientation:
matrices represent row activities as `y = A x`.

## Invariants

- Row count and column count are non-negative.
- Values, index arrays, and pointer arrays are copied on construction.
- Public array accessors must return defensive copies.
- Pointer arrays are monotonic.
- Pointer array length is dimension + 1.
- First pointer is zero.
- Last pointer equals `values.length`.
- Index arrays have the same length as `values`.
- Indices are within matrix shape.
- Duplicate structural entries are not coalesced by storage constructors; any
  builder that accepts duplicate coordinates must combine or reject them before
  materialization.
- Indices within a row or column should be emitted in ascending order by
  builders and writers. Storage constructors validate shape, not sort order,
  so fixture code remains explicit and cheap.

## Performance posture

Do not add object-heavy sparse entries to hot paths. Any higher-level builder
must materialize primitive arrays before validation or solve execution.

Builders may use temporary collections while constructing tiny fixtures or
parsing files, but the materialized matrix used by validation, solver adapters,
and solver kernels must be CSR or CSC primitive storage.

## Operations

The required 0.1.0 kernel is dense matrix-vector multiplication:

- CSR computes `y = A x`.
- CSC computes `y = A x`.

Both operations reject input vectors whose length does not equal the matrix
column count. Additional operations should be added only when required by
fixtures, validation, MPS I/O, or solver tasks.
