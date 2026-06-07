# mundane Java LP

`mundane-java-lp` is a design-first Java 21 / GraalVM Native Image project for
studying massive sparse linear programming behavior.

The 0.1.0 product is a reproducible LP correctness and benchmark harness with
external solver adapters, Java-library solver adapters, and two in-project
experimental solver adapters evaluated through the same validation path.

## Development model

This repository is intended to be coding-agent friendly and design-control
oriented. Human maintainers set the direction, approve architecture gates, and
review roadmap tasks; implementation work must be traceable to requirements,
module boundaries, tests, and documentation.

Contributor-facing documentation is part of the product, not an afterthought.

## Current repository phase

G0 through G10 implementation-readiness work is complete. The repository is in
0.1.0 release hardening: public API documentation has been reviewed, release
documentation is being reconciled, and quality-gate/release-readiness work
remains tracked in `docs/roadmap/roadmap-index.md`.

Implemented for the 0.1.0 readiness baseline:

- Gradle Groovy DSL multi-module structure under `modules/` and `examples`.
- Local `build-logic` convention plugins and architecture tests.
- Canonical LP model, primitive sparse storage, MPS read/write support,
  validation, harness orchestration, and deterministic JSON/CSV/Markdown
  reports.
- CLI adapters for HiGHS, CLP, and GLPK; Java-library adapters for OR-Tools
  and ojAlgo.
- In-project simple and performance-oriented experimental solver adapters.
- Generated fixtures, curated public benchmark manifests, solver-comparison
  smoke, strict comparison, expanded benchmark, profiling, and native smoke
  lanes.

The in-project solvers are release-ready only as narrow experimental adapters.
Correctness claims require `lp-validation` acceptance, and benchmark output is
evidence collection rather than a solver-speed claim.

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

## Solver comparison targets

Comparison targets:

- HiGHS CLI adapter.
- COIN-OR CLP CLI adapter.
- GLPK CLI adapter.
- OR-Tools Java adapter.
- ojAlgo adapter.
- In-project simple solver adapter.
- In-project performance-oriented solver adapter.

Adapters are intentionally isolated in separate modules. External solvers are
optional and must be reported as unavailable rather than failing unrelated
quality gates. Strict evidence lanes require the provisioned solver toolchain.
Current adapter status, supported exclusions, and comparison evidence are
tracked in
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
./gradlew strictSolverComparison --console=plain
./gradlew expandedBenchmarkSuite --console=plain
```

Public benchmark and solver-speed claims remain limited by the recorded
evidence in `docs/verification/performance-evidence-report.md` and
`docs/verification/performance-readiness-review.md`.

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
