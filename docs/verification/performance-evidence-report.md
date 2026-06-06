# Performance evidence report

G10-007 records the current 0.1.0 performance-evidence snapshot. This is an
evidence report, not a public performance claim.

## Evidence Status

Benchmark evidence is **incomplete** for release-performance claims.

The strict solver comparison lane passed with all required solvers available.
The expanded benchmark lane passed as a reporting lane, but the three curated
Netlib public benchmark candidates currently produce deterministic
`ADAPTER_ERROR` records because the 0.1.0 MPS reader cannot load those full
Netlib files. Generated benchmark evidence is valid; public benchmark runtime
evidence is not yet valid.

## Commands

```bash
PATH="/tmp/mlp-micromamba-root/envs/mlp-solvers/bin:$PATH" \
  ./gradlew strictSolverComparison expandedBenchmarkSuite --console=plain

tools/fetch-public-benchmarks.sh

./gradlew :modules:lp-harness-cli:profileExpandedRun --console=plain
```

The full task acceptance command remains:

```bash
PATH="/tmp/mlp-micromamba-root/envs/mlp-solvers/bin:$PATH" \
  ./gradlew validateDesignControlPack qualityGate strictSolverComparison expandedBenchmarkSuite --console=plain
```

## Report Artifacts

- Strict solver comparison Markdown/JSON/CSV:
  `examples/solver-comparison-smoke/build/reports/strict-solver-comparison/`
- Expanded benchmark Markdown/JSON/CSV:
  `modules/lp-harness-cli/build/reports/expanded-benchmark-suite/`
- JVM profiling artifact:
  `modules/lp-harness-cli/build/reports/expanded-benchmark-profile/mlp-expanded-benchmark.jfr`

## Strict Solver Comparison

`strictSolverComparison` ran 16 deterministic correctness instances through the
required solver set: HiGHS, CLP, GLPK, OR-Tools, ojAlgo, the simple solver, and
the performance solver.

Summary:

| Solver | Version or path evidence | Records | Success | Unsupported | Failures | Solve seconds range |
|---|---|---:|---:|---:|---:|---:|
| HiGHS | HiGHS 1.14.0 at `/tmp/mlp-micromamba-root/envs/mlp-solvers/bin/highs` | 16 | 12 | 4 | 0 | 0.026098767-0.066869205 |
| CLP | Coin LP 1.17.11 at `/tmp/mlp-micromamba-root/envs/mlp-solvers/bin/clp` | 16 | 10 | 6 | 0 | 0.041218107-0.099416286 |
| GLPK | GLPSOL 5.0 at `/tmp/mlp-micromamba-root/envs/mlp-solvers/bin/glpsol` | 16 | 12 | 4 | 0 | 0.015156458-0.026135461 |
| OR-Tools | Gradle dependency `9.15.6755` | 16 | 15 | 1 | 0 | 0.000112114-0.357395544 |
| ojAlgo | Gradle dependency `56.2.1` | 16 | 16 | 0 | 0 | 0.000194456-0.842953844 |
| Simple solver | In-project adapter | 16 | 16 | 0 | 0 | 0.000008952-0.009204931 |
| Performance solver | In-project adapter | 16 | 10 | 6 | 0 | 0.000016634-0.022188083 |

Overall strict comparison counts:

- records: 112;
- successful validated records: 91;
- unsupported solver/instance records: 21;
- unavailable solvers: 0;
- adapter errors: 0;
- validation failures: 0.

Unsupported records are expected 0.1.0 subset boundaries. They remain visible
in the reports and are not counted as accepted solves.

## Expanded Benchmark Suite

`expandedBenchmarkSuite` ran six deterministic generated benchmark families and
three curated public Netlib manifest entries.

Summary:

| Suite | Solver | Records | Success | Adapter errors | Notes |
|---|---|---:|---:|---:|---|
| `expanded-benchmark-generated` | performance | 6 | 6 | 0 | Generated dense, sparse, network-like, equality-heavy, degenerate, and scaled cases validated. |
| `expanded-benchmark-generated` | generated-evidence | 6 | 6 | 0 | Fixture sanity adapter validated expected evidence. |
| `expanded-benchmark-public` | public-benchmark | 3 | 0 | 3 | Netlib AFIRO, ADLITTLE, and SCORPION are present locally but not loadable by the 0.1.0 MPS subset. |

Expanded benchmark public failures:

- `netlib-afiro`: malformed for current MPS subset, record outside supported
  section;
- `netlib-adlittle`: malformed for current MPS subset, record outside supported
  section;
- `netlib-scorpion`: malformed for current MPS subset, record outside supported
  section.

Generated benchmark timing evidence is valid for instrumentation and regression
tracking. It is not enough for public comparative claims because the public
benchmark lane is not yet producing validated runtime records.

## Required Evidence Fields

The report formats include these fields before any future performance claim is
evaluated:

- mode, suite, instance, and solver identity;
- solver version and binary path or dependency/in-project diagnostic;
- normalized solver status, objective evidence, and termination diagnostic;
- validation acceptance, tolerance profile, and residual/finding summary;
- solver options: thread count and time limit;
- parse/load, export/canonicalization, solve, validation, and total timing
  buckets, with `not-measured` for missing buckets;
- warmup count, repetition count, min/median/max solve-time summaries, failure
  count, and unavailable count;
- peak memory field, currently `not-measured`;
- machine metadata: operating system, architecture, Java version, and processor
  count;
- failure, unsupported, and unavailable records instead of silent skips.

## Profiling Evidence

The JVM profiling workflow produced a Java Flight Recorder artifact for the
expanded benchmark lane:

`modules/lp-harness-cli/build/reports/expanded-benchmark-profile/mlp-expanded-benchmark.jfr`

Native optimized and PGO profile modes are wired through the GraalVM Native
Build Tools plugin, but no native profiling artifact is required for this
snapshot because local `native-image` is unavailable.

## Limitations

- The strict comparison suite is correctness-oriented; it is not a benchmark
  methodology for solver speed.
- Each benchmark group currently has warmup count `0` and repetition count `1`.
- Peak memory is not measured.
- Parse/load and export timing remain `not-measured` for current generated
  benchmark rows.
- The public Netlib benchmark candidates are downloaded and checksum-verified,
  but the 0.1.0 MPS reader cannot yet load them.
- No conclusion should be drawn that one solver is faster or more robust than
  another from this snapshot.
