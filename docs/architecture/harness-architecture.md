# Harness architecture

The harness orchestrates instances, solvers, options, validation, and reports.

```text
BenchmarkSuite
    -> BenchmarkPlan
        -> SolverAdapter.solve(...)
            -> SolverRunResult
                -> ValidationReport
                    -> RunRecord
                        -> Markdown/JSON/CSV report
```

## Inputs

The 0.1.0 harness consumes explicit inputs only:

- benchmark suites with stable suite and instance IDs;
- benchmark instances carrying canonical model metadata, sparse coefficients,
  row names, column names, and expected validation evidence;
- an explicit list of solver adapters; no automatic classpath discovery;
- solver options including time limit and thread count;
- a bounded work-directory root;
- a validation tolerance profile;
- selected report formats and output directory.

The harness must not invent mathematical evidence. It may adapt canonical Tier 1
fixtures into benchmark instances, but validation expectations stay sourced from
fixture evidence.

## Execution Plan

A harness run expands a suite and solver list into deterministic run requests.
Request order is suite order first, then solver order. Each request owns a
dedicated child work directory under the configured work root.

```text
BenchmarkSuite
    -> BenchmarkInstance
    -> SolverRunRequest
    -> LpSolverAdapter.solve(...)
    -> SolverRunResult
    -> LpSolutionValidator
    -> RunRecord
    -> Markdown/JSON/CSV report
```

The API module owns suite, instance, request, execution, machine metadata, and
run-record types. The CLI module wires explicit small commands to those APIs.
The report module renders already-recorded run records; it does not execute
solvers or perform validation.

## Run Records

Every solver-instance request produces one run record. Records capture:

- suite ID and instance ID;
- solver ID and normalized solver status;
- solver version when available;
- solver objective and diagnostic message when supplied;
- validation accepted/rejected state and findings;
- validation tolerance profile;
- residual summary fields when validation computes them;
- solver options;
- timing buckets for parse, export, solve, validation, and total wall time;
- machine metadata;
- failure category for adapter errors, unavailable solvers, or harness errors.

Unavailable solvers are records, not omitted runs. Adapter exceptions are caught
and converted to deterministic failure records.

Fields that are not measured in 0.1.0 must still render deterministically as
`not-measured` or empty numeric fields, depending on report format. Reports must
not silently drop required benchmark-suite columns.

## Work Directories

The harness creates per-run work directories below a caller-supplied root. It
must not write outside that root. Directory names are derived from sanitized
suite, instance, and solver IDs plus a deterministic sequence number when
needed. Cleanup policy is caller-controlled so failed runs can be inspected.

## CLI Contract

The CLI stays small and explicit:

- `--help` prints command usage;
- the default smoke path runs a tiny built-in suite with explicit test/dummy
  adapters until real adapter wiring exists;
- later solver comparison commands list adapters explicitly.

The CLI must not discover adapters from the classpath or environment implicitly.

## Verification Lanes

Default local lanes are:

- local quality lane: `validateDesignControlPack qualityGate`;
- solver comparison lane: `solverComparisonSmoke`;
- benchmark lane: future generated/public benchmark smoke tasks;
- native lane: future native-image smoke tasks.

Solver comparison, benchmark, and native lanes may report unavailable optional
tools, but the local quality lane must stay deterministic without external
solver binaries.
