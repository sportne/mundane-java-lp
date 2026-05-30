# G0-008 doc cleanup review

## Status

proposed

## Requirement IDs

- REQ-0-1-DOC-CLEANUP-REVIEW

## References

- `docs/roadmap/tasks/g0-004-doc-placeholder-inventory.md`
- `docs/roadmap/tasks/g0-005-doc-consolidation-pass.md`

## Target module

- Repository documentation.

## Allowed files

- `docs/`
- `README.md`
- Module and example `README.md` files.

## Forbidden files

- Production Java source.
- Gradle build files.
- `docs/roadmap/tasks/`

## Required behavior

- Review documentation after design decisions and remove duplication.
- Consolidate stale design prose into the authoritative document for each
  topic.
- Keep task files intact.

## Required tests

- Run a placeholder/deferred/stale-language search and document intentional
  remaining cases.
- Check links from roadmap, README, and module READMEs.

## Required docs

- Update docs changed by design tasks to match final 0.1.0 design language.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
