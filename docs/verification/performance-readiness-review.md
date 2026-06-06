# Performance readiness review

G9-019 closes the first in-project performance solver experiment. The decision
is that `lp-solver-performance` meets the 0.1.0 credible-but-modest bar as an
experimental solver with validation evidence, benchmark smoke wiring, and a
recorded limitation set.

This is not a public performance claim. The solver is ready to ship in 0.1.0 as
an explicitly narrow in-project implementation whose behavior is documented,
validated on supported fixtures, and reported through the harness.

## Decision

The performance solver is ready for the 0.1.0 release under these conditions:

- it remains labelled as the in-project performance-oriented experiment;
- solver output is trusted only after `lp-validation` accepts the evidence;
- unsupported model shapes return deterministic `UNSUPPORTED` results;
- benchmark output is treated as evidence collection, not comparative proof;
- future broadening must happen through later roadmap work instead of expanding
  the 0.1.0 release scope.

## Supported cases

The 0.1.0 solver supports the subset chosen in
`docs/adr/ADR-0007-performance-solver-approach.md`:

- continuous variables with zero lower bounds;
- finite variable upper bounds;
- finite upper rows;
- finite lower rows through artificial variables;
- simple equality rows;
- minimization and maximization through objective normalization;
- normalized optimal, infeasible, unbounded, and unsupported outcomes;
- primitive-array tableau state exposed only through `lp-solver-spi` results.

The supported Tier 1 fixtures are:

- `single-bounded-variable`;
- `two-variable-feasible-optimum`;
- `unbounded-nonnegative-ray`;
- `redundant-row`;
- `equality-row`;
- `degenerate-optimum`.

The generated smoke fixture `network-flow-3-node-seed-7` is also supported by
the benchmark smoke lane and validates with accepted objective and primal
evidence.

## Unsupported cases

The following cases are intentionally outside the 0.1.0 performance solver
subset:

- free variables;
- shifted variable lower bounds;
- ranged-row normalization;
- general presolve;
- broad scaling or equilibration;
- warm starts;
- large public benchmark execution as a release requirement;
- numerical robustness or performance superiority claims.

Tier 1 fixtures requiring free variables, shifted lower bounds, or ranged rows
are recorded as deterministic `UNSUPPORTED` outcomes. The numerical stress
ranged case carries feasible evidence but remains unsupported by this solver
until ranged-row normalization is designed.

## Evidence

The readiness decision is based on these completed records:

- correctness coverage in `docs/verification/correctness-suite.md`;
- profiling and optimization notes in
  `docs/verification/performance-profiling-baseline.md`;
- G10 evidence in `docs/verification/performance-evidence-report.md`;
- report fields for validation, timing, options, machine metadata, unavailable
  solvers, and missing public inputs;
- local acceptance with:

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke nativeSmoke --console=plain
```

The evidence demonstrates that the performance solver can participate in the
same harness and reporting lanes as the other adapters. It does not demonstrate
that the solver is faster, more robust, or more scalable than another solver.

## G10 Evidence Update

G10 expanded the evidence lanes beyond the original smoke snapshot:

- `strictSolverComparison` now runs all required solvers over the expanded
  correctness suite and records solver versions and binary paths;
- `expandedBenchmarkSuite` runs generated benchmark families and curated public
  Netlib manifest entries;
- `profileExpandedBenchmarkSuite` produces JVM profiling artifacts for the
  expanded benchmark lane.

The G10 evidence report is currently incomplete for public performance claims
because the downloaded Netlib candidates produce deterministic MPS load errors
under the 0.1.0 reader. That does not invalidate generated benchmark or strict
solver-comparison evidence, but it blocks treating the benchmark evidence as
release-complete.

## G10 Evidence Readiness Decision

The project can enter release hardening for 0.1.0 implementation readiness, but
not for public benchmark-evidence readiness.

Release hardening can start because:

- all required comparison solvers are available in the strict evidence
  environment;
- `strictSolverComparison` passes with no unavailable solvers, adapter errors,
  or validation failures;
- unsupported solver/fixture combinations are deterministic records rather than
  skipped evidence;
- generated expanded benchmark families validate through the harness;
- JVM profiling and native optimized/PGO workflows are documented and wired.

Benchmark-evidence readiness remains blocked because:

- curated public Netlib files are present and checksum-verified but cannot be
  parsed by the 0.1.0 MPS subset;
- public benchmark rows are `ADAPTER_ERROR`, so they cannot support runtime
  comparisons;
- benchmark groups currently have warmup count `0` and repetition count `1`;
- peak memory, parse/load, and export/canonicalization timing are still
  `not-measured` for generated benchmark evidence;
- no native profiling artifact was produced because local `native-image` is
  unavailable.

## Release posture

For 0.1.0, the performance solver remains release-ready as an experimental,
evidence-producing adapter. The project may proceed to release-hardening tasks
that verify public API, documentation, quality gates, and packaging. It should
not make public benchmark or solver-speed claims until the public benchmark
blockers above are resolved and rerun through the evidence lanes.
