# lp-harness-api

Benchmark and solver-run orchestration API module.

`lp-harness-api` owns benchmark suites, benchmark instances, solver run
requests, run records, machine metadata, and the runner that coordinates adapter
execution with validation. It records every solver-instance request, including
unavailable solvers and adapter failures.

The runner executes explicit adapter lists in suite order and creates bounded
per-run work directories under the caller-provided work root.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
