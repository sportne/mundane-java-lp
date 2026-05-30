# G0-004 doc placeholder inventory

## Status

proposed

## Requirement IDs

- REQ-0-1-DOC-INVENTORY

## References

- `docs/charter.md`
- `docs/literature/literature-index.md`
- `docs/verification/verification-strategy.md`

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

- Inventory placeholder, deferred, future, stale, or duplicate documentation.
- Classify each item as keep, fill, merge, or remove.
- Identify which later task owns each fill or cleanup action.

## Required tests

- Run a repository search for placeholder and deferred language and document
  remaining intentional occurrences.

## Required docs

- Add or update a documentation inventory section in roadmap or documentation
  maintenance docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
