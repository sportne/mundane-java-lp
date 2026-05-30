# G4-005 performance solver core

## Status

proposed

## Requirement IDs

- REQ-V1-PERFORMANCE-SOLVER-CORE

## References

- `docs/roadmap/tasks/g1-008-performance-solver-design-decision.md`
- `docs/literature/sparse-linear-algebra.md`
- `docs/literature/numerical-stability.md`

## Target module

- Performance solver module.

## Allowed files

- Performance solver module.
- `modules/lp-sparse/` only for contract-driven additions.
- Performance solver docs and README.

## Forbidden files

- Simple solver behavior changes.
- Third-party adapter modules.
- Harness behavior except test wiring.

## Required behavior

- Implement the approved performance solver core for the v1.0 supported
  problem subset.
- Use primitive sparse data structures and avoid unnecessary allocation.
- Return normalized statuses and solution evidence through the solver SPI.

## Required tests

- Unit tests for solver core steps required by the approved design.
- Correctness smoke tests on small deterministic fixtures.
- Negative tests for unsupported problem shapes.

## Required docs

- Update performance solver README with supported scope and limitations.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
