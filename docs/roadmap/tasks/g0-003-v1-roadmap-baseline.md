# G0-003 v1 roadmap baseline

## Status

proposed

## Requirement IDs

- REQ-V1-ROADMAP

## References

- `docs/charter.md`
- `docs/scope.md`
- `docs/roadmap/roadmap-index.md`
- `docs/roadmap/implementation-task-template.md`

## Target module

- Repository roadmap documentation.

## Allowed files

- `docs/roadmap/roadmap-index.md`
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Gradle build files.
- Completed task files unless correcting a broken link.

## Required behavior

- Define the version 1.0.0 requirement set, phase gates, dependency order, and
  task naming rules.
- Keep the roadmap waterfall-oriented: design and documentation, fixtures and
  harness, third-party adapters, in-project solvers, then release hardening.
- State that task files are exempt from future markdown consolidation.

## Required tests

- Check every v1.0 task link in `docs/roadmap/roadmap-index.md` resolves to a
  local task file.
- Verify every task file keeps the approved template headings.

## Required docs

- Update `docs/roadmap/roadmap-index.md`.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
