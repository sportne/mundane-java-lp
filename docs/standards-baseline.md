# Standards and compatibility baseline

This project has no standards conformance claim in G0. The baseline is an
engineering contract for files, mathematical forms, statuses, and benchmarks.

## Initial LP form

The canonical model targets bounded linear programs with row constraints:

```text
minimize or maximize cᵀx
subject to           lhs <= A x <= rhs
                     lower <= x <= upper
```

This form is chosen because it can represent equalities, ranged rows, lower and
upper variable bounds, fixed variables, and free variables.

The 0.1.0 canonical model records objective sense, objective constant, dense
objective coefficients, variable bounds, row activity bounds, matrix shape, and
a diagnostic problem name. Row and variable names are positional metadata
carried by fixture or I/O envelopes when needed. Matrix coefficients are carried
by sparse storage or I/O envelopes outside `lp-model` so the model remains
independent of solver, I/O, and storage implementation details.

Invalid mathematical data is rejected by production constructors. Invalid
fixtures are allowed only as boundary-specific test data and must not be mixed
into valid fixture catalogs.

## File-format baseline

- MPS support is the first interchange target.
- LP text format is deferred until after MPS round-trip behavior is tested.
- Parser diagnostics must be bounded and deterministic.

## Solver-status baseline

Solver-specific statuses are normalized to:

- `OPTIMAL`
- `FEASIBLE`
- `INFEASIBLE`
- `UNBOUNDED`
- `INFEASIBLE_OR_UNBOUNDED`
- `TIME_LIMIT`
- `MEMORY_LIMIT`
- `NUMERICAL_FAILURE`
- `ERROR`
- `UNSUPPORTED`
- `UNKNOWN`

## Correctness baseline

Solver-reported status is not sufficient. The harness validates the returned
solution against objective, bound, row-activity, dual, and gap checks as those
validators become available.
