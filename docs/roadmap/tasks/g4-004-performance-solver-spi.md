# G4-004 performance solver SPI

## Status

proposed

## Requirement IDs

- REQ-V1-PERFORMANCE-SOLVER-SPI

## References

- `docs/roadmap/tasks/g1-008-performance-solver-design-decision.md`
- `docs/architecture/solver-adapter-contract.md`
- `docs/architecture/module-boundaries.md`

## Target module

- Future performance solver module.
- `modules/lp-solver-spi`

## Allowed files

- New performance solver module files.
- `settings.gradle`
- `build.gradle`
- `modules/lp-bom/build.gradle`
- `docs/architecture/module-boundaries.md`
- Relevant README files.

## Forbidden files

- Simple solver behavior changes except shared test fixtures.
- Third-party adapter behavior.

## Required behavior

- Add the performance-oriented in-project solver as a solver SPI
  implementation.
- Keep module wiring minimal and aligned with native-image constraints.
- Do not make performance claims in this task.

## Required tests

- Module wiring or smoke tests proving the solver can return a normalized result
  for a trivial fixture.
- Architecture tests for dependency direction and native-friendly constraints.

## Required docs

- Update module boundaries, solver docs, and published artifact docs if needed.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
