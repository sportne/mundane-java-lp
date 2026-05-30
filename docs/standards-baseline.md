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

The 0.1.0 MPS subset supports deterministic free-field-style records for
continuous LP fixtures:

- `NAME`
- `ROWS`
- `COLUMNS`
- `RHS`
- optional `BOUNDS`
- `ENDATA`

Supported row types are `N` for the single objective row, `E` for equality rows,
`L` for less-than-or-equal rows, and `G` for greater-than-or-equal rows. The
0.1.0 parser does not support the MPS `RANGES` section. Canonical ranged rows
are therefore outside the MPS round-trip fixture subset until a later task adds
deterministic ranged-row encoding.

MPS files in the 0.1.0 subset are interpreted as minimization problems. Writing
a maximization canonical fixture to MPS is unsupported unless the fixture has
already been transformed to equivalent minimization coefficients outside
`lp-io-mps`.

Supported bounds are continuous `LO`, `UP`, `FX`, `FR`, `MI`, and `PL` bounds
for at most one bound set. Missing variable-bound records and missing `BOUNDS`
sections both default variables to `0 <= x <= +infinity`, matching traditional
MPS LP behavior at the file boundary. The canonical model must still record
explicit bounds after parsing.

Unsupported MPS features must fail with deterministic `MpsFormatException`
messages instead of being ignored:

- integer markers and integer/binary bound types;
- multiple objective rows;
- multiple RHS or bound sets;
- `RANGES`, `SOS`, `QMATRIX`, `QSECTION`, `QUADOBJ`, indicator, basis, and
  solution sections;
- row or column references to names not declared in the required sections;
- malformed records or missing required `NAME`, `ROWS`, `COLUMNS`, `RHS`, or
  `ENDATA` sections.

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
