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

G0 includes the types and CLI entrypoint needed to make this shape explicit.
Future gates fill in execution behavior.
