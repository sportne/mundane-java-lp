# lp-io-mps

MPS format read/write module.

`lp-io-mps` supports the initial 0.1.0 LP interchange subset for
hand-checkable fixtures:

- sections: `NAME`, `ROWS`, `COLUMNS`, `RHS`, optional `BOUNDS`, `ENDATA`;
- one objective row with row type `N`;
- row types `E`, `L`, and `G`;
- continuous bounds `LO`, `UP`, `FX`, `FR`, `MI`, and `PL`;
- deterministic parse errors for malformed or unsupported input.

Unsupported features include integer markers, integer/binary bound types,
multiple objective rows, maximization models that have not already been
converted to minimization, multiple RHS or bound sets, `RANGES`, SOS and
quadratic sections, basis/solution sections, and references to undeclared row or
column names.

Round trips are expected to preserve the canonical LP math and supported row,
column, and objective names for fixtures in the supported subset. Ranged
canonical rows are excluded from 0.1.0 MPS round-trip fixtures because `RANGES`
is intentionally outside the subset.

The main API exposes `MpsLp`, an I/O envelope that carries the canonical
`LpProblem`, CSR coefficients, row names, column names, and objective row name.
`MpsReader.read(Path)` remains available for callers that only need problem
metadata.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
