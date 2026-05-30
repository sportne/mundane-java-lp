# G3-002 validation engine fixtures

## Status

proposed

## Requirement IDs

- REQ-0-1-VALIDATION-FIXTURES

## References

- `docs/architecture/result-validation.md`
- `docs/roadmap/tasks/g3-001-validation-design-completion.md`
- `docs/verification/correctness-suite.md`

## Target module

- `modules/lp-validation`
- `modules/lp-testkit`

## Allowed files

- `modules/lp-validation/`
- `modules/lp-testkit/`
- `docs/architecture/result-validation.md`
- `docs/verification/correctness-suite.md`

## Forbidden files

- Solver and adapter modules.
- Harness execution implementation.

## Required behavior

- Implement validation fixtures for bounds, row activity, objective values,
  normalized statuses, and gap checks required by 0.1.0.
- Ensure findings are deterministic and include useful codes and magnitudes.
- Keep validation independent from solver-specific output.

## Required tests

- Unit tests for accepted and rejected cases for each validation dimension.
- Tolerance profile tests for boundary behavior.

## Required docs

- Update validation architecture, correctness suite, and module README.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
