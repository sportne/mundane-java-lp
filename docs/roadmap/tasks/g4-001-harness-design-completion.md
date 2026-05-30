# G4-001 harness design completion

## Status

complete

## Requirement IDs

- REQ-0-1-HARNESS-DESIGN

## References

- `docs/architecture/harness-architecture.md`
- `docs/verification/benchmark-suite.md`
- `docs/verification/verification-strategy.md`

## Target module

- `modules/lp-harness-api`
- `modules/lp-harness-cli`
- `modules/lp-harness-report`

## Allowed files

- `docs/architecture/harness-architecture.md`
- `docs/verification/benchmark-suite.md`
- Harness module `README.md` files.
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Solver and adapter modules.

## Required behavior

- Define harness inputs, execution plans, run records, work directories,
  unavailable solver handling, and report requirements.
- Keep the CLI small and explicit; no automatic classpath discovery.
- Define default local, solver comparison, benchmark, and native lanes.

## Required tests

- No production tests required; this is a design task.
- Confirm later harness implementation tasks reference these behaviors.

## Required docs

- Update harness architecture, benchmark suite, verification strategy, and
  harness module READMEs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
