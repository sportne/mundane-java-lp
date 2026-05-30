# G3-009 harness doc cleanup

## Status

proposed

## Requirement IDs

- REQ-V1-HARNESS-DOC-CLEANUP

## References

- `docs/roadmap/tasks/g3-001-harness-execution-model.md`
- `docs/roadmap/tasks/g3-008-solver-comparison-smoke.md`

## Target module

- Harness and adapter documentation.

## Allowed files

- `docs/architecture/harness-architecture.md`
- `docs/verification/`
- Harness and adapter module READMEs.
- Example READMEs.

## Forbidden files

- Production Java source.
- Gradle build files.
- `docs/roadmap/tasks/`

## Required behavior

- Align harness, report, adapter, and solver comparison docs with implemented
  behavior.
- Remove obsolete placeholder wording and duplicate command descriptions.
- Keep docs concise and operational.

## Required tests

- Search for stale placeholder language in harness and adapter docs.
- Confirm documented commands match Gradle task names.

## Required docs

- Update harness, report, adapter, verification, and example docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke --console=plain
```
