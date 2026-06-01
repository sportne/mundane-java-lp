# Verification strategy

The project separates correctness, performance, native-image, and benchmark
claims into explicit lanes.

## Default local lane

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```

This lane should remain suitable for normal development and must not require
external solver binaries.

## Native lane

```bash
./gradlew nativeSmoke --console=plain
```

Native checks are explicit because they require GraalVM/native-image.

## Solver comparison lane

```bash
./gradlew solverComparisonSmoke --console=plain
```

This lane compares the tiny Tier 1 smoke instance through HiGHS, CLP, GLPK,
OR-Tools, ojAlgo, and the in-project simple and performance solver adapters.
Missing external binaries or unavailable optional runtimes are reported as
unavailable solver outcomes. The lane still passes when optional third-party
solvers are unavailable, but it fails when an available solver returns an
adapter error or fails validation.

## Benchmark lane

```bash
./gradlew benchmarkSmoke --console=plain
```

Benchmark smoke is advisory only. It does not support competitive claims.
Its generated fixture path runs the in-project performance solver without
requiring external solver binaries.

The performance solver readiness review is recorded in
`performance-readiness-review.md`. It treats benchmark smoke as release evidence
for wiring, validation, and reporting only.

## Manual future lanes

- `publicBenchmarkSuite`
- `generatedLargeSuite`
- `numericalStressSuite`
- `massiveSuite`
