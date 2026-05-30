# G0-002 restore acceptance gate

## Status

complete

## Requirement IDs

- REQ-G0-ACCEPTANCE-GATE

## References

- `build-logic/src/main/groovy/mlp.docs-validation-conventions.gradle`
- `modules/lp-sparse/src/test/java/io/github/mundanej/mlp/sparse/CscMatrixTest.java`
- `modules/lp-generators/src/main/java/io/github/mundanej/mlp/generators/TinyLpGenerator.java`

## Target module

- Root build logic.
- `lp-sparse`.
- `lp-generators`.

## Allowed files

- `build-logic/src/main/groovy/mlp.docs-validation-conventions.gradle`
- `modules/lp-sparse/src/test/java/io/github/mundanej/mlp/sparse/CscMatrixTest.java`
- `modules/lp-generators/src/main/java/io/github/mundanej/mlp/generators/TinyLpGenerator.java`
- `docs/roadmap/tasks/g0-002-restore-acceptance-gate.md`

## Forbidden files

- `LICENSE`

## Required behavior

Restore the G0 acceptance gate after scaffold drift:

- `validateDesignControlPack` must be compatible with Gradle configuration
  cache.
- CSC multiplication tests must assert the value represented by the fixture.
- Javadoc for tiny LP fixtures must not emit raw inequality warnings.

## Required tests

- `./gradlew validateDesignControlPack --console=plain`
- `./gradlew :modules:lp-sparse:test --tests io.github.mundanej.mlp.sparse.CscMatrixTest --console=plain`
- `./gradlew validateDesignControlPack qualityGate --console=plain`

## Required docs

- Add this task record.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
