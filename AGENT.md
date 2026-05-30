# Coding-agent instructions

This repository is design-control oriented. Do not begin implementation work
unless the task names an approved roadmap item or explicitly states that it is a
scaffold-only change.

## Required task fields

Every implementation task must include:

1. requirement IDs;
2. literature, benchmark, or standards references where applicable;
3. target module;
4. allowed files and forbidden files;
5. required tests;
6. required documentation updates;
7. exact acceptance command.

## Default constraints

- Keep production code in Java 21.
- Use Gradle Groovy DSL.
- Keep published/internal Java code under `modules/`.
- Keep non-published demonstrations under `examples/`.
- Do not add runtime reflection, classpath scanning, dynamic class loading,
  dynamic proxies, serialization, JNI, `Unsafe`, or internal JDK APIs to
  native-targeted modules without an ADR.
- Do not put solver-specific logic in `lp-model`, `lp-sparse`, or
  `lp-validation`.
- Do not make performance claims without recorded benchmark evidence.
- Do not make correctness claims from solver status alone; validation evidence
  must be recorded.

## Default acceptance command

For design and scaffold changes:

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```

For Native Image changes:

```bash
./gradlew nativeSmoke --console=plain
```

For benchmark-harness changes:

```bash
./gradlew solverComparisonSmoke benchmarkSmoke --console=plain
```
