# G2-004 validation engine fixtures

## Status

proposed

## Requirement IDs

- REQ-V1-VALIDATION-FIXTURES

## References

- `docs/architecture/result-validation.md`
- `docs/roadmap/tasks/g1-004-validation-design-completion.md`
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
  normalized statuses, and gap checks required by v1.0.
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
