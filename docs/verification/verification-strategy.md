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

## Strict solver comparison lane

```bash
./gradlew strictSolverComparison --console=plain
```

This lane requires the provisioned solver toolchain and fails when any required
solver is unavailable. The required set is HiGHS, CLP, GLPK, OR-Tools, ojAlgo,
the simple solver, and the performance solver. It is the CI evidence lane after
external solver setup; local development can continue to use
`solverComparisonSmoke` when external binaries are not installed.

## Benchmark lane

```bash
./gradlew benchmarkSmoke --console=plain
./gradlew expandedBenchmarkSuite --console=plain
```

Benchmark smoke is advisory only. It does not support competitive claims.
Its generated fixture path runs the in-project performance solver without
requiring external solver binaries.

`expandedBenchmarkSuite` is the fuller benchmark evidence lane. It runs the
expanded generated family set and the curated public manifest. CI downloads and
verifies public benchmark files before this lane; local runs without downloads
record deterministic missing-input rows.

The performance solver readiness review is recorded in
`performance-readiness-review.md`. It now separates implementation readiness
from benchmark-evidence readiness: release hardening may proceed, but public
benchmark or solver-speed claims remain blocked until all listed readiness
blockers are resolved and rerun through the evidence lanes.

## Profiling lane

```bash
./gradlew profileExpandedBenchmarkSuite --console=plain
```

This lane runs the expanded benchmark suite with Java Flight Recorder enabled
and writes the profile artifact next to the benchmark reports. Native optimized
and PGO build experiments are selected with `mlp.native.profile` on the
plugin-backed `nativeCompile`/`nativeRun` tasks. Profiling is optional and is not
part of the default quality gate.

## Manual reserve lanes

- `generatedLargeSuite`
- `numericalStressSuite`
- `massiveSuite`
