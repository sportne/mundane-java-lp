# G9-007 performance solver core

## Status

proposed

## Requirement IDs

- REQ-0-1-PERFORMANCE-SOLVER-CORE

## References

- `docs/roadmap/tasks/g9-005-performance-solver-design-decision.md`
- `docs/adr/ADR-0007-performance-solver-approach.md`
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

- Implement the approved performance solver core for the 0.1.0 supported
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
