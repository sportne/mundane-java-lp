# lp-solver-performance

Performance-oriented in-project solver module.

The G9 performance solver follows
`docs/adr/ADR-0007-performance-solver-approach.md`. The first SPI task wires
the module and exposes a deterministic adapter identity plus a trivial empty-LP
smoke path. Algorithmic revised-simplex behavior is intentionally added by later
G9 tasks.

The module must not execute external processes or depend on third-party solver
libraries. Solver state should remain primitive-array based as the core lands.

See the repository root `README.md`,
`docs/architecture/module-boundaries.md`, and
`docs/architecture/solver-adapter-contract.md`.
