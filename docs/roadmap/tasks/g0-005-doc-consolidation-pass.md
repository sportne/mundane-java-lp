# G0-005 doc consolidation pass

## Status

complete

## Requirement IDs

- REQ-0-1-DOC-CONSOLIDATION

## References

- `docs/charter.md`
- `docs/scope.md`
- `docs/roadmap/tasks/g0-004-doc-placeholder-inventory.md`

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

- Remove or merge markdown files that no longer serve the current project
  state.
- Preserve small, boring, direct documentation over duplicated prose.
- Keep task files untouched except for link updates required by moved docs.

## Required tests

- Confirm all links from `README.md`, `AGENT.md`, and roadmap docs still
  resolve.
- Run a placeholder-language search and confirm no unowned stale placeholders
  remain.

## Required docs

- Update affected documentation indexes after consolidation.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
