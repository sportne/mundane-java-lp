# release-001 public API Javadoc review

## Status

proposed

## Requirement IDs

- REQ-0-1-PUBLIC-API-JAVADOC

## References

- `docs/roadmap/tasks/g0-006-javadoc-quality-baseline.md`
- `docs/architecture/architecture-rule-catalog.md`

## Target module

- All published modules.

## Allowed files

- `modules/`
- `examples/`
- Public API docs and READMEs.

## Forbidden files

- Behavioral changes not required by documentation correctness.
- New public APIs.

## Required behavior

- Complete public API Javadocs for classes, methods, fields, parameters,
  records, enum constants, and exceptions.
- Remove misleading comments and replace them with concise behavior docs.
- Keep Javadocs useful, not verbose.

## Required tests

- All Javadoc tasks must pass without new warnings.
- Existing tests must keep passing.

## Required docs

- Update contributor or architecture docs if the Javadoc baseline changed.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
