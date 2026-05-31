# lp-solver-simple

Correctness-first in-project solver module.

The G9 simple solver follows
`docs/adr/ADR-0006-simple-solver-approach.md`: it is deterministic, tiny, and
optimized for Tier 1 evidence clarity rather than speed.

The 0.1.0 implementation supports:

- zero-variable feasibility and objective constants;
- one-variable interval solving with finite or infinite variable and row
  bounds;
- two-variable vertex enumeration for variable bounds, equality rows, lower
  rows, upper rows, ranged rows, fixed variables, and simple unbounded rays;
- deterministic `INFEASIBLE`, `UNBOUNDED`, `OPTIMAL`, and `UNSUPPORTED`
  normalized statuses.

The solver returns `UNSUPPORTED` for models with more than two variables. It
does not claim numerical robustness, broad LP coverage, or performance. Tests
validate the supported Tier 1 fixture outputs through `lp-validation`.

`examples/tiny-lp` demonstrates the explicit MPS path: write a supported Tier 1
fixture with `lp-io-mps`, read it back, and run this adapter through
`lp-harness-api`.

See the repository root `README.md`,
`docs/architecture/module-boundaries.md`, and
`docs/architecture/solver-adapter-contract.md`.
