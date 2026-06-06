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

## Release posture

For 0.1.0, the performance solver is release-ready as an experimental,
evidence-producing adapter. It should be described as useful for internal
validation, instrumentation, and future optimization work. It should not be
described as a production LP solver or as competitive with established solvers.
