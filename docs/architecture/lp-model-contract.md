# LP model contract

The canonical model represents a linear program as:

```text
sense: MINIMIZE or MAXIMIZE
objective: c and constant
rows: lhs <= A x <= rhs
bounds: lower <= x <= upper
```

## Invariants

- Variable count and bound vector lengths match.
- Row count and row-bound vector lengths match.
- Sparse matrix dimensions match row and variable counts.
- Lower bounds may be negative infinity.
- Upper bounds may be positive infinity.
- Lower bound must not exceed upper bound except in intentionally invalid test
  fixtures.

## G0 limitation

G0 model classes capture shape and metadata but do not yet embed the sparse
matrix directly. That is introduced in the G1/G2 implementation tasks.
