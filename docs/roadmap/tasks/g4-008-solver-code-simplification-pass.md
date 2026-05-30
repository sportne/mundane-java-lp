# G4-008 solver code simplification pass

## Status

proposed

## Requirement IDs

- REQ-V1-SOLVER-SIMPLIFICATION

## References

- `docs/charter.md`
- `docs/architecture/module-boundaries.md`

## Target module

- Simple solver module.
- Performance solver module.
- Shared solver test fixtures.

## Allowed files

- In-project solver modules.
- `modules/lp-testkit/`
- Solver READMEs.

## Forbidden files

- Third-party adapter behavior.
- Public API expansion unless it removes duplication.

## Required behavior

- Remove duplicated solver setup, stale helpers, and unnecessary abstractions.
- Prefer net fewer lines of solver support code where practical.
- Keep simple and performance solvers independently understandable.

## Required tests

- Existing solver correctness and benchmark smoke tests must keep passing.
- Review diff for simplification rather than feature expansion.

## Required docs

- Update solver READMEs if simplification changes supported usage.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke --console=plain
```
