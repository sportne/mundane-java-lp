# Profiling workflow

G10 profiling is optional tooling for choosing optimization targets. It does not
change benchmark validation behavior and it does not establish performance
claims by itself.

## JVM Profiling

Run the expanded benchmark suite with Java Flight Recorder:

```bash
./gradlew profileExpandedBenchmarkSuite --console=plain
```

The task delegates to `:modules:lp-harness-cli:profileExpandedRun`, runs the same
expanded benchmark mode as `expandedBenchmarkSuite`, and writes normal benchmark
reports plus a JFR artifact:

- reports: `modules/lp-harness-cli/build/reports/expanded-benchmark-profile/`
- profile: `modules/lp-harness-cli/build/reports/expanded-benchmark-profile/mlp-expanded-benchmark.jfr`

The console output records the profiler, JVM flags, suite mode, warmup count,
repetition count, and profile artifact path. Use the JFR file to identify CPU
hot spots, allocation hot spots, parsing/export overhead, validation overhead,
and solver-core hot paths before selecting a performance task.

## Native Profiling

Native executable builds remain owned by the GraalVM Native Build Tools Gradle
plugin. The `mlp.native.profile` property selects the build profile used by the
plugin-backed `nativeCompile` task:

| Profile | Native Build Tools build arguments | Runtime training arguments | Purpose |
|---|---|---|---|
| `smoke` | none beyond the normal `--no-fallback` plugin configuration | none | Fast smoke build |
| `optimized` | `-O3` | none | Optimized native performance experiment |
| `pgo-instrument` | `-O3 --pgo-instrument` | `-XX:ProfilesDumpFile=<path>` | Build and run an instrumented executable for training |
| `pgo-optimized` | `-O3 --pgo=<path>` | none | Rebuild with collected PGO data |

Print the selected native profile metadata without requiring GraalVM:

```bash
./gradlew :examples:native-cli-smoke:nativeProfileMetadata \
  -Pmlp.native.profile=optimized \
  --console=plain
```

Run an optimized native build when GraalVM is available:

```bash
./gradlew :examples:native-cli-smoke:nativeCompile \
  -Pmlp.native.profile=optimized \
  --console=plain
```

Collect and use PGO data with an explicit profile path:

```bash
./gradlew :examples:native-cli-smoke:nativeCompile \
  -Pmlp.native.profile=pgo-instrument \
  -Pmlp.native.pgoProfile=build/native/pgo/mlp-training.iprof \
  --console=plain

./gradlew :examples:native-cli-smoke:nativeRun \
  -Pmlp.native.profile=pgo-instrument \
  -Pmlp.native.pgoProfile=build/native/pgo/mlp-training.iprof \
  --console=plain

./gradlew :examples:native-cli-smoke:nativeCompile \
  -Pmlp.native.profile=pgo-optimized \
  -Pmlp.native.pgoProfile=build/native/pgo/mlp-training.iprof \
  --console=plain
```

PGO profile data is a local build artifact and must stay out of source control
unless a separate task defines a committed fixture policy.

## Optimization Target Selection

Optimization work should cite profiler artifacts and benchmark report rows. A
credible target names the affected benchmark suite, solver set, profiler
artifact path, observed hot method or allocation site, and the validation status
of the measured runs.
