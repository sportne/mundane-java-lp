# lp-testkit

Shared test fixture and assertion helper module.

`lp-testkit` exposes the shared Tier 1 fixture catalog for downstream module
tests. It should wrap stable fixture access and assertion helpers rather than
create new mathematical examples independently.

The module also exposes adapters from canonical fixture evidence to
`lp-validation` expected/evidence records so validation and harness tests use the
same hand-checked Tier 1 data.

Prefer `LpTestInstances.tierOneFixtures()` and `tierOneFixture(name)` in
downstream tests instead of depending on generator catalog internals directly.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
