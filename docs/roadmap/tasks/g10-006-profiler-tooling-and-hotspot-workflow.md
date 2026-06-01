# G10-006 profiler tooling and hotspot workflow

## Status

proposed

## Requirement IDs

- REQ-0-1-PROFILER-TOOLING

## References

- `docs/roadmap/tasks/g9-011-performance-solver-profiling-baseline.md`
- `docs/roadmap/tasks/g10-004-expanded-generated-and-public-benchmarks.md`
- `docs/roadmap/tasks/g10-005-benchmark-methodology-and-statistics.md`
- `docs/literature/benchmark-methodology.md`
- `docs/architecture/native-image.md`

## Target module

- Benchmark harness tooling.
- Native smoke tooling.
- Performance documentation.

## Allowed files

- `modules/lp-harness-cli/`
- `modules/lp-harness-api/`
- `modules/lp-harness-report/`
- `examples/`
- Build logic for profiling tasks.
- `docs/verification/`
- `docs/architecture/native-image.md`
- `docs/literature/benchmark-methodology.md`

## Forbidden files

- Solver algorithm changes.
- Performance claims from a single profiler run.
- Mandatory dependence on a proprietary profiler.

## Required behavior

- Add a repeatable JVM profiling workflow for benchmark suites, using an
  open-source profiler option where practical.
- Support profiler attachment or wrapper execution without changing benchmark
  correctness behavior.
- Record profiler command, JVM flags, benchmark suite, solver list, warmup and
  repetition settings, output artifact paths, and environment metadata.
- Define how profiler outputs map to optimization targets, including allocation
  hot spots, CPU hot spots, parser/export overhead, validation overhead, and
  solver core hot paths.
- Add a native-image profiling path where available, such as a native executable
  benchmark profile that can be inspected with platform profilers or GraalVM
  profiling support.
- Add an optimized native-image benchmark build profile for performance
  experiments, separate from the normal fast smoke build.
- Add a PGO workflow for native-image performance experiments: build an
  instrumented executable, run representative training suites to collect profile
  data, rebuild with the collected profile, and record the profile artifact and
  command metadata.
- Keep PGO profile data out of source control unless a later task explicitly
  defines a small committed fixture policy.
- Keep profiling optional locally and explicit in CI; normal quality gates must
  not require profiler binaries.

## Required tests

- Tests for profiler command construction without requiring the profiler binary.
- Tests that profiling mode preserves benchmark suite ordering and report
  metadata.
- Tests that absent profiler tools produce deterministic unavailable diagnostics
  instead of failing normal benchmark smoke.
- Native profiling tests must skip cleanly when native-image or the platform
  profiler is unavailable.
- Native optimized and PGO command-construction tests must not require GraalVM
  locally; they should verify Gradle task wiring, arguments, output paths, and
  skip behavior with toolchain doubles or TestKit fixtures.

## Required docs

- Document JVM profiling setup, native profiling setup, optimized native-image
  builds, PGO collection/rebuild workflow, expected artifacts, and how to select
  optimization targets from recorded hot spots.
- Update benchmark methodology docs to require profiler evidence before future
  optimization tasks claim a targeted hot path.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke nativeSmoke --console=plain
```
