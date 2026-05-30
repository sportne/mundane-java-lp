# Sparse matrix contract

Sparse matrix code must use explicit primitive storage.

Initial storage formats:

- CSC: `double[] values`, `int[] rowIndices`, `int[] columnPointers`.
- CSR: `double[] values`, `int[] columnIndices`, `int[] rowPointers`.

## Invariants

- Pointer arrays are monotonic.
- Pointer array length is dimension + 1.
- Last pointer equals `values.length`.
- Index arrays have the same length as `values`.
- Indices are within matrix shape.

## Performance posture

Do not add object-heavy sparse entries to hot paths. Any higher-level builder
must materialize primitive arrays before validation or solve execution.
