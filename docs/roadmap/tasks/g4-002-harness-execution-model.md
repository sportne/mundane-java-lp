# G4-002 harness execution model

## Status

proposed

## Requirement IDs

- REQ-0-1-HARNESS-EXECUTION

## References

- `docs/architecture/harness-architecture.md`
- `docs/roadmap/tasks/g4-001-harness-design-completion.md`
- `docs/verification/verification-strategy.md`

## Target module

- `modules/lp-harness-api`
- `modules/lp-harness-cli`

## Allowed files

- `modules/lp-harness-api/`
- `modules/lp-harness-cli/`
- `modules/lp-testkit/`
- Harness docs and READMEs.

## Forbidden files

- Adapter implementation modules except for test doubles.
- Solver implementation modules.

## Required behavior

- Implement real harness execution from suite to adapter call to validation to
  run record.
- Preserve bounded work-directory behavior and deterministic failure records.
- Keep CLI commands explicit and small.

## Required tests

- Unit tests for successful runs, validation failures, adapter errors, and
  unavailable solver records using test doubles.
- CLI smoke tests for the default tiny suite.

## Required docs

- Update harness architecture, verification strategy, and harness module
  READMEs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
