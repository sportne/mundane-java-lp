# Performance evidence report

G9-017 records the first 0.1.0 performance evidence snapshot. This report is a
smoke-evidence record, not a public performance claim. It compares the simple
solver, performance solver, and third-party adapters only where the adapters are
available in the local environment.

## Commands

```bash
./gradlew solverComparisonSmoke benchmarkSmoke --console=plain
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke nativeSmoke --console=plain
```

The full gate was the commit acceptance command. `nativeSmoke` skipped native
executable work when `native-image` was unavailable, which is expected for this
lane.

## Solver comparison evidence

`solverComparisonSmoke` runs the `single-bounded-variable` Tier 1 fixture
through the explicit solver list:

- HiGHS CLI;
- CLP CLI;
- GLPK CLI;
- OR-Tools Java;
- ojAlgo;
- in-project simple solver;
- in-project performance solver.

The local snapshot produced seven solver-instance records. HiGHS, CLP, and GLPK
were reported as `SOLVER_UNAVAILABLE` because their CLI binaries were not
available. OR-Tools, ojAlgo, the simple solver, and the performance solver
returned accepted validation evidence for the fixture.

The generated reports are written under
`examples/solver-comparison-smoke/build/reports/solver-comparison-smoke/` when
the example runs directly and through the root `solverComparisonSmoke` lane.

Snapshot row evidence:

| Solver | Status | Outcome | Accepted | Objective | Residuals | Solve seconds | Validation seconds | Total seconds | Version | Threads | Time limit | Peak memory | Machine | Termination |
|---|---|---|---:|---:|---|---:|---:|---:|---|---:|---:|---|---|---|
| HiGHS CLI | `UNSUPPORTED` | `SOLVER_UNAVAILABLE` | false | | 1 | 0.079166626 | 0.01172232 | 0.131100547 | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | HiGHS binary unavailable: highs |
| CLP CLI | `UNSUPPORTED` | `SOLVER_UNAVAILABLE` | false | | 1 | 0.058576657 | 0.000019274 | 0.061928273 | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | CLP binary unavailable: clp |
| GLPK CLI | `UNSUPPORTED` | `SOLVER_UNAVAILABLE` | false | | 1 | 0.072651602 | 0.000022124 | 0.076256151 | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | GLPK binary unavailable: glpsol |
| OR-Tools Java | `OPTIMAL` | `SUCCESS` | true | 0.0 | none | 0.395729656 | 0.000891568 | 0.402107657 | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | OR-Tools GLOP status: OPTIMAL |
| ojAlgo | `OPTIMAL` | `SUCCESS` | true | 0.0 | none | 0.700289104 | 0.000027452 | 0.703644667 | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | ojAlgo state: DISTINCT |
| Simple solver | `OPTIMAL` | `SUCCESS` | true | 0.0 | none | 0.005719971 | 0.000030925 | 0.009188327 | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | one-dimensional LP optimal |
| Performance solver | `OPTIMAL` | `SUCCESS` | true | 0.0 | none | 0.015798537 | 0.000035661 | 0.020088372 | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | simplex core optimal |

Parse/load and export/canonicalization seconds were `not-measured` for every
solver-comparison row.

## Benchmark smoke evidence

`benchmarkSmoke` runs the generated three-node network-flow fixture through the
in-project performance solver and the generated-evidence adapter. It also checks
the curated public manifest entries without vendoring public benchmark files.

The local snapshot produced five benchmark records: two accepted generated
records and three deterministic missing-public-input records. Missing public
files are evidence records, not failures, because the public benchmark curation
policy keeps downloaded benchmark files out of git.

The generated reports are written under
`modules/lp-harness-cli/build/reports/benchmark-smoke/` for the root lane. Each
row uses the same Markdown, JSON, and CSV evidence fields as solver comparison
reports.

Snapshot row evidence:

| Suite | Instance | Solver | Status | Outcome | Accepted | Objective | Residuals | Solve seconds | Validation seconds | Total seconds | Version | Threads | Time limit | Peak memory | Machine | Termination |
|---|---|---|---|---|---:|---:|---|---:|---:|---:|---|---:|---:|---|---|---|
| `benchmark-smoke-generated` | `network-flow-3-node-seed-7` | performance | `OPTIMAL` | `SUCCESS` | true | 6.0 | none | 0.018648067 | 0.00728461 | 0.063212986 | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | simplex core optimal |
| `benchmark-smoke-generated` | `network-flow-3-node-seed-7` | generated-evidence | `OPTIMAL` | `SUCCESS` | true | 6.0 | none | 0.0 | 0.000023882 | 0.00521822 | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | built-in generated evidence adapter |
| `benchmark-smoke-public` | `netlib-afiro` | public-benchmark | `UNSUPPORTED` | `SOLVER_UNAVAILABLE` | false | | 1 | 0.0 | `not-measured` | `not-measured` | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | missing local public benchmark file: `instances/public/netlib/afiro.mps` |
| `benchmark-smoke-public` | `netlib-adlittle` | public-benchmark | `UNSUPPORTED` | `SOLVER_UNAVAILABLE` | false | | 1 | 0.0 | `not-measured` | `not-measured` | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | missing local public benchmark file: `instances/public/netlib/adlittle.mps` |
| `benchmark-smoke-public` | `netlib-scorpion` | public-benchmark | `UNSUPPORTED` | `SOLVER_UNAVAILABLE` | false | | 1 | 0.0 | `not-measured` | `not-measured` | `not-measured` | 1 | 60 | `not-measured` | Linux amd64, Java 21.0.11, 32 processors | missing local public benchmark file: `instances/public/netlib/scorpion.mps` |

Parse/load and export/canonicalization seconds were `not-measured` for every
benchmark-smoke row.

## Required evidence fields

The report formats must keep these fields visible before any future performance
claim can be evaluated:

- instance provenance through suite and instance identifiers;
- solver identity, version field, normalized status, objective evidence, and
  termination diagnostics;
- validation acceptance, tolerance profile, and residual/finding summary;
- solver options, including thread count and time limit;
- parse/load, export/canonicalization, solve, validation, and total timing
  buckets, with `not-measured` where a bucket is unavailable;
- peak memory evidence, currently `not-measured` in smoke lanes;
- machine metadata: operating system, architecture, Java version, and available
  processor count;
- failure and unavailable records instead of silent skips.

## Limitations

This evidence is intentionally small:

- the solver-comparison lane uses one tiny Tier 1 fixture;
- the benchmark lane uses one generated fixture plus manifest checks;
- external CLI solvers may be unavailable;
- peak memory is not measured;
- no warm-up, repetition, statistical summary, or public benchmark runtime
  comparison is recorded.

The evidence supports smoke-level correctness and reporting confidence only. It
does not support claims that one solver is faster, more robust, or more scalable
than another.

G9-019 uses this report as the performance readiness evidence snapshot. The
readiness decision is recorded in
`docs/verification/performance-readiness-review.md`.
