# mundane Java LP

`mundane-java-lp` is a design-first Java 21 / GraalVM Native Image project for
studying massive sparse linear programming behavior.

The initial product is a reproducible LP correctness and benchmark harness.
Java-native LP solver implementations are roadmap work and will be evaluated
through the same harness as established open-source solvers.

## Development model

This repository is intended to be coding-agent friendly and design-control
oriented. Human maintainers set the direction, approve architecture gates, and
review roadmap tasks; implementation work must be traceable to requirements,
module boundaries, tests, and documentation.

Contributor-facing documentation is part of the product, not an afterthought.

## Current repository phase

This scaffold is in **G0: design-control scaffold and harness foundation**.

Implemented in this scaffold:

- Gradle Groovy DSL multi-module structure under `modules/` and `examples/`.
- Local `build-logic/` convention plugins.
- Core module boundaries for model, sparse matrix, validation, I/O, harness,
  solver adapters, native API boundaries, and architecture tests.
- Lightweight Java source skeletons that compile once dependencies are
  resolved by Gradle.
- Documentation baseline for scope, architecture, verification, literature,
  and roadmap gates.
- Root verification task names matching the mundane project style:
  `checkAll`, `qualityGate`, `nativeSmoke`, `benchmarkSmoke`,
  `solverComparisonSmoke`, and `printPublishedArtifacts`.

No production LP solver is implemented in G0.

## Goals

- Represent sparse LPs explicitly and deterministically.
- Read and write initial MPS test instances.
- Compare LP solver results through a common harness.
- Validate primal feasibility, dual feasibility, objective values, gaps, and
  status claims independently of solver logs.
- Benchmark correctness and performance across problem families.
- Keep core modules friendly to GraalVM Native Image.
- Eventually publish JVM jars, native executables, and native shared libraries.

## Non-goals

- MIP support in the initial project.
- A full algebraic modeling language in the initial project.
- Competing with commercial solvers on all LPs.
- Treating solver-reported status as sufficient correctness evidence.
- Runtime reflection, classpath scanning, dynamic plugin discovery, or hidden
  dependency discovery in native-targeted core modules.

## Baseline solver comparison targets

Initial comparison targets:

- HiGHS CLI adapter.
- COIN-OR CLP CLI adapter.
- GLPK CLI adapter.
- OR-Tools Java adapter.
- ojAlgo adapter.

Adapters are intentionally isolated in separate modules. External solvers are
optional and must be reported as unavailable rather than failing unrelated
quality gates. Current adapter status is tracked in
`docs/verification/solver-comparison.md`.

## Common project checks

After adding a Gradle wrapper or using a local Gradle installation, run:

```bash
./gradlew projects --console=plain
./gradlew validateDesignControlPack --console=plain
./gradlew qualityGate --console=plain
./gradlew printPublishedArtifacts --console=plain
```

Native and benchmark lanes are intentionally explicit:

```bash
./gradlew nativeSmoke --console=plain
./gradlew solverComparisonSmoke --console=plain
./gradlew benchmarkSmoke --console=plain
```

## Build layout

Build conventions live in `build-logic/` as composable Gradle convention
plugins. Published and internal library modules live under `modules/`;
non-published examples live under `examples/`.

See:

- `docs/charter.md`
- `docs/scope.md`
- `docs/architecture/module-boundaries.md`
- `docs/verification/verification-strategy.md`
- `docs/roadmap/roadmap-index.md`

## Agent rule

No implementation task may begin without:

1. requirement IDs;
2. literature or standards references where applicable;
3. target module;
4. allowed and forbidden files;
5. required tests;
6. required documentation updates;
7. an exact acceptance command.

See `AGENT.md` and `docs/roadmap/implementation-task-template.md`.
