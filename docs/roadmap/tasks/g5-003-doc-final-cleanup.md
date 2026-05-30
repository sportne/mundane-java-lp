# G5-003 doc final cleanup

## Status

proposed

## Requirement IDs

- REQ-V1-DOC-FINAL-CLEANUP

## References

- `docs/roadmap/tasks/g0-004-doc-placeholder-inventory.md`
- `docs/roadmap/tasks/g1-010-doc-cleanup-review.md`
- `docs/roadmap/tasks/g3-009-harness-doc-cleanup.md`

## Target module

- Repository documentation.

## Allowed files

- `docs/`
- `README.md`
- `AGENT.md`
- Module and example `README.md` files.

## Forbidden files

- Production Java source.
- Gradle build files.
- `docs/roadmap/tasks/`

## Required behavior

- Remove stale placeholders and reconcile README, module READMEs, architecture,
  verification, and literature docs with v1.0 behavior.
- Consolidate duplicate markdown files where the simpler structure is clear.
- Keep task files exempt from consolidation.

## Required tests

- Search for placeholder, deferred, and future wording and document intentional
  remaining cases.
- Confirm key docs link to existing files.

## Required docs

- Update all affected docs to v1.0 release language.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
