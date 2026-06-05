# G8-002 allocation and native friendliness pass

## Status

complete

## Requirement IDs

- REQ-0-1-ALLOCATION-NATIVE-FRIENDLINESS

## References

- `docs/architecture/native-image.md`
- `docs/architecture/architecture-rule-catalog.md`
- `docs/roadmap/tasks/g8-001-native-image-smoke.md`

## Target module

- Native-targeted modules and solver hot paths.

## Allowed files

- Native-targeted modules.
- In-project solver modules.
- `modules/lp-sparse/`
- Native and architecture docs.

## Forbidden files

- Third-party adapter internals.
- Public API expansion without a documented need.

## Required behavior

- Reduce avoidable allocation in native-targeted hot paths.
- Preserve no-reflection, no-dynamic-loading, no-serialization, and no-unsafe
  constraints.
- Prefer small direct changes over new abstraction.

## Required tests

- Existing quality gate and native smoke must pass.
- Architecture tests must cover any new native-friendly constraints.

## Required docs

- Update native architecture and architecture rule catalog if constraints
  change.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate nativeSmoke --console=plain
```
