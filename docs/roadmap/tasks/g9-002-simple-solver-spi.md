# G9-002 simple solver SPI

## Status

complete

## Requirement IDs

- REQ-0-1-SIMPLE-SOLVER-SPI

## References

- `docs/roadmap/tasks/g9-001-simple-solver-design-decision.md`
- `docs/architecture/solver-adapter-contract.md`
- `docs/architecture/module-boundaries.md`

## Target module

- Future simple solver module.
- `modules/lp-solver-spi`

## Allowed files

- New simple solver module files.
- `settings.gradle`
- `build.gradle`
- `modules/lp-bom/build.gradle`
- `docs/architecture/module-boundaries.md`
- Relevant README files.

## Forbidden files

- Performance solver implementation.
- Third-party adapter behavior.

## Required behavior

- Add the simple in-project solver as a solver SPI implementation.
- Keep module wiring minimal and consistent with existing Gradle conventions.
- Do not implement broad algorithm behavior beyond a small SPI smoke path.

## Required tests

- Module wiring tests or smoke tests proving the solver can return a normalized
  result for a trivial fixture.
- Architecture tests for dependency direction.

## Required docs

- Update module boundaries, solver docs, and published artifact docs if needed.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
