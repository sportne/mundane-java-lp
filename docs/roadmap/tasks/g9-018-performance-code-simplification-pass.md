# G9-018 performance code simplification pass

## Status

complete

## Requirement IDs

- REQ-0-1-PERFORMANCE-SOLVER-SIMPLIFICATION

## References

- `docs/charter.md`
- `docs/architecture/module-boundaries.md`

## Target module

- Performance solver module.
- Shared performance solver test fixtures.

## Allowed files

- Performance solver module.
- `modules/lp-testkit/`
- Performance solver README.

## Forbidden files

- Third-party adapter behavior.
- Public API expansion unless it removes duplication.

## Required behavior

- Remove duplicated performance solver setup, stale helpers, and unnecessary
  abstractions.
- Prefer net fewer lines of solver support code where practical.
- Keep performance solver improvements independently understandable.

## Required tests

- Existing performance solver correctness and benchmark smoke tests must keep
  passing.
- Review diff for simplification rather than feature expansion.

## Required docs

- Update performance solver README if simplification changes supported usage.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke --console=plain
```
