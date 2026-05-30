# G9-003 simple solver correctness

## Status

proposed

## Requirement IDs

- REQ-0-1-SIMPLE-SOLVER-CORRECTNESS

## References

- `docs/roadmap/tasks/g9-001-simple-solver-design-decision.md`
- `docs/verification/correctness-suite.md`
- `docs/architecture/result-validation.md`

## Target module

- Simple solver module.

## Allowed files

- Simple solver module.
- `modules/lp-testkit/`
- `docs/verification/correctness-suite.md`
- Simple solver README.

## Forbidden files

- Performance solver module.
- Third-party adapter modules.

## Required behavior

- Implement the approved simple solver enough to correctly solve Tier 1
  fixtures in scope for 0.1.0.
- Return normalized statuses and solution evidence through the solver SPI.
- Prefer clarity over performance.

## Required tests

- Correctness tests for all supported Tier 1 fixtures.
- Validation tests proving solver outputs are independently accepted.
- Unsupported-feature tests for fixtures outside the simple solver scope.

## Required docs

- Update correctness suite and simple solver README with supported scope and
  non-goals.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
