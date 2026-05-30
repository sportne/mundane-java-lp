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

This lane must skip or mark unavailable solvers cleanly when external binaries
are absent.

## Benchmark lane

```bash
./gradlew benchmarkSmoke --console=plain
```

G0 benchmark smoke is advisory only. It does not support competitive claims.

## Manual future lanes

- `publicBenchmarkSuite`
- `generatedLargeSuite`
- `numericalStressSuite`
- `massiveSuite`
