# lp-generators

Deterministic LP fixture and instance generator module.

The initial fixture suite covers Tier 1 hand-checkable LP shapes from the
correctness suite. Each canonical fixture carries:

- an `LpProblem` descriptor;
- a primitive sparse coefficient matrix;
- positional row and column names for diagnostics and file I/O;
- expected objective/primal evidence for optimal cases, or explicit
  infeasible/unbounded outcome evidence.

Fixtures are intentionally small and directly constructed so reviews can check
the math without trusting a solver.

`BenchmarkFixtureGenerator` adds the expanded generated benchmark baseline:
small dense, sparse rectangular, network-like, equality-heavy, degenerate, and
scaled cases. These instances are still deterministic and hand-checkable; they
exist to broaden benchmark evidence, not to support performance claims by
themselves.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
