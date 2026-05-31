# lp-solver-simple

Correctness-first in-project solver module.

The G9 simple solver follows
`docs/adr/ADR-0006-simple-solver-approach.md`: it is deterministic, tiny, and
optimized for Tier 1 evidence clarity rather than speed. G9-002 only wires the
module and SPI adapter with a trivial smoke path. Broader low-dimensional
correctness behavior lands in G9-003.

See the repository root `README.md`,
`docs/architecture/module-boundaries.md`, and
`docs/architecture/solver-adapter-contract.md`.
