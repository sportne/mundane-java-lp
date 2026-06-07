# LP model contract

The canonical model is the solver-neutral LP description used by fixtures,
file I/O, validation, solver adapters, and harness records. It deliberately
does not describe a solver algorithm or interchange-file syntax.

For 0.1.0, a canonical LP is:

```text
sense: MINIMIZE or MAXIMIZE
objective: constant + c^T x
rows: lhs <= A x <= rhs
bounds: lower <= x <= upper
```

`A` is a sparse row-by-column coefficient matrix owned outside `lp-model`.
`lp-model` records the problem shape, objective vector, variable bounds, row
bounds, and problem name; storage modules decide how matrix coefficients are
materialized.

## Invariants

- Problem names are non-blank and stable within reports and fixture output.
- Objective sense is explicit: `MINIMIZE` and `MAXIMIZE` are never inferred
  from solver defaults.
- Objective constants are part of the objective value calculation.
- Objective coefficient count equals the variable count.
- Variable count and bound-vector length match.
- Row count and row-bound-vector length match.
- Sparse matrix dimensions match row and variable counts.
- Sparse matrix nonzero count matches the recorded problem statistics.
- Lower bounds may be negative infinity.
- Upper bounds may be positive infinity.
- Free variables use `[-infinity, +infinity]`.
- Fixed variables use equal finite lower and upper bounds.
- Free rows use `[-infinity, +infinity]`.
- Equality rows use equal finite lower and upper activity bounds.
- Less-than rows use `[-infinity, rhs]`.
- Greater-than rows use `[lhs, +infinity]`.
- Ranged rows use finite `lhs < rhs`.
- Lower bound must not exceed upper bound except in intentionally invalid test
  fixtures.
- Finite numeric values must remain deterministic Java `double` values. NaN is
  not valid 0.1.0 model data and must be rejected before a fixture enters a
  shared valid-fixture catalog.

## Names

Problem names are diagnostic identifiers. They are not a semantic part of the
mathematical model.

Row and variable names are positional metadata carried by fixture or I/O
envelopes when needed. They are not owned by `lp-model` in 0.1.0. Readers and
writers may preserve row and variable names when a format supports them, but
solvers must use row and variable position as the canonical identity.

MPS-compatible fixtures should use names that can be written without escaping
or format-specific remapping. Richer input formats may carry display names
separately from canonical positional identity.

MPS round-trip fixtures for 0.1.0 exclude canonical ranged rows because the MPS
subset intentionally excludes the `RANGES` section. A separate task may add a
deterministic ranged-row encoding, but it must define how row names and
canonical row identity survive the file boundary.

## Invalid fixtures

Normal production constructors reject shape and bound inconsistencies. Tests
that need malformed input should build invalid data at the boundary under test,
such as a parser fixture or sparse-storage constructor call, rather than
weakening the canonical model invariants.

When a test needs intentionally invalid LP semantics, it must name the
fixture as invalid and keep it out of shared valid-fixture catalogs.
