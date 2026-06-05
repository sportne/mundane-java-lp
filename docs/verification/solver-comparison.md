# Solver comparison

Comparison targets:

- HiGHS CLI.
- COIN-OR CLP CLI.
- GLPK CLI.
- OR-Tools Java.
- ojAlgo.
- In-project simple solver.
- In-project performance solver.

G5 implements the CLI adapter lane for HiGHS, CLP, and GLPK. G6 owns Java
library adapter behavior for OR-Tools and ojAlgo. G9 adds the in-project
simple and performance solvers to the explicit smoke list.

## Fairness rules

- Use the same canonical model for all solvers.
- Record any format conversion required by an adapter.
- Report unavailable solvers instead of silently omitting them.
- Record all failures.
- Validate solver outputs independently.
- Record versions and machine metadata.
- Record timeout/thread options and timing buckets with `not-measured` where a
  bucket is not implemented yet.
- Keep validation failures in the report instead of filtering them from
  comparison summaries.
- Do not hide unfavorable results.

Solver comparison smoke is correctness-oriented evidence. It may show that
adapters can run the same tiny instance and produce validated results, but it is
not enough for solver performance claims.

## CLI Adapter Baseline

The CLI comparison suite uses the same solver input envelope for every CLI
adapter. Each adapter exports the model to its own work directory, captures a
bounded diagnostic, normalizes status, and reports unavailable binaries as
records instead of skipped runs.

The comparison lane includes Java library adapters for OR-Tools and ojAlgo.
These adapters use the same solver input envelope as the CLI adapters, but they
solve in process and keep third-party dependencies isolated to their own
modules. OR-Tools native-runtime load failures and unavailable optional solver
capabilities are reported as unavailable solver records.

The comparison smoke runs one tiny Tier 1 instance through HiGHS, CLP, GLPK,
OR-Tools, ojAlgo, the simple solver, and the performance solver. It writes
Markdown, JSON, and CSV reports under
`examples/solver-comparison-smoke/build/reports/solver-comparison-smoke` for
both direct example-project runs and the root `solverComparisonSmoke` lane. It
passes when optional third-party solvers are unavailable, provided each
unavailable solver is reported explicitly and in-project solvers validate. If
any available solver returns an adapter error or fails validation, the lane
fails after writing reports.

The first G9 performance evidence snapshot for this lane is recorded in
`docs/verification/performance-evidence-report.md`. It is smoke evidence only
and does not make a solver-speed claim.

## Toolchain Provisioning

CI runs the comparison lane on `ubuntu-24.04` with the external CLI solvers
installed before Gradle verification:

- HiGHS CLI `highs` is installed from the pinned upstream release archive
  `v1.14.0/highs-1.14.0-x86_64-linux-gnu-static-mit.tar.gz`.
- CLP CLI `clp` is installed from the Ubuntu `coinor-clp` package.
- GLPK CLI `glpsol` is installed from the Ubuntu `glpk-utils` package.

OR-Tools Java and ojAlgo remain Gradle-managed library adapters. Their versions
are pinned in `gradle/libs.versions.toml` as
`com.google.ortools:ortools-java:9.15.6755` and
`org.ojalgo:ojalgo:56.2.1`.

Developers can inspect local solver availability without mutating the machine:

```bash
./gradlew verifySolverToolchain --console=plain
```

The task prints each solver name, command path, version output when available,
and a deterministic unavailable diagnostic when a command is absent. CI runs the
same verifier with `MLP_SOLVER_TOOLCHAIN_STRICT=true` after installing the
external binaries so missing required commands fail the evidence lane.
