# G6-001 Java adapter design completion

## Status

proposed

## Requirement IDs

- REQ-0-1-JAVA-ADAPTER-DESIGN

## References

- `docs/architecture/solver-adapter-contract.md`
- `docs/verification/solver-comparison.md`
- `docs/roadmap/tasks/g5-001-cli-adapter-design-completion.md`

## Target module

- Java library adapter modules.

## Allowed files

- `docs/architecture/solver-adapter-contract.md`
- `docs/verification/solver-comparison.md`
- Java adapter module `README.md` files.
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Build dependency changes.
- CLI adapter behavior.

## Required behavior

- Define Java library adapter responsibilities for OR-Tools and ojAlgo.
- Define dependency isolation, native-runtime handling, unsupported-feature
  reporting, and normalized result mapping.
- Keep Java adapters optional and isolated from foundation modules.

## Required tests

- No production tests required; this is a design task.
- Confirm Java adapter implementation tasks reference this design.

## Required docs

- Update solver adapter contract, solver comparison docs, and Java adapter
  READMEs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
