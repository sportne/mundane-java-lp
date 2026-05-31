# Performance solver approach

## Status

Accepted for G9 performance solver work.

## Context

The project charter says the first product is a correctness and benchmark
harness, not a solver. G9 is the first narrow experiment that may add an
in-project solver, and ADR-0006 already keeps the simple solver intentionally
small and evidence-oriented.

The next solver needs a different purpose: exercise primitive sparse data flow,
allocation discipline, measurement, and native-friendly implementation choices
without claiming broad LP support. The design must stay small enough that every
supported result can be validated by the existing harness and reported with the
evidence baseline.

Interior-point and first-order methods remain credible future directions, but
both require substantially more numerical infrastructure before they would be
honest in this codebase. The 0.1.0 performance solver should instead choose an
approach whose moving parts can be inspected in unit tests and profiled with
small deterministic fixtures.

## Decision

Implement a narrow revised-simplex-style continuous LP solver in a separate
`lp-solver-performance` module.

The 0.1.0 implementation is restricted to a modest canonical subset:

- continuous variables only;
- minimization and maximization through explicit objective normalization;
- nonnegative variables, with finite upper bounds handled only where the solver
  can normalize them predictably;
- finite `<=` rows and simple equality rows through a minimal Phase I path;
- primitive-array tableau, basis, and workspace state;
- normalized terminal statuses through `lp-solver-spi`;
- no public performance claim without validation, timing, solver options,
  machine metadata, and failure or unsupported records in harness reports.

Sparse data enters through the existing CSR/CSC contracts. The solver may
materialize small dense work arrays internally for the approved subset, but
steady-state solver data structures must remain primitive arrays and
caller-owned buffers where practical.

## Non-goals

- general-purpose LP support;
- mixed-integer, quadratic, SOS, or ranged-row solver support;
- robust presolve;
- advanced scaling;
- dual certificate completeness;
- basis warm starts;
- numerical superiority claims;
- third-party solver dependencies;
- external process execution.

## Numerical risks

The first implementation is expected to be fragile near degeneracy, tight
tolerances, ill-conditioned rows, and badly scaled coefficients. Those outcomes
must be recorded as validation failures, deterministic unsupported statuses, or
documented limitations rather than hidden by looser tolerances.

Pivot selection, ratio tests, Phase I feasibility, objective reconstruction, and
termination tolerances require focused tests before benchmark evidence is
interpretable. G9 stress and iteration tasks must update this decision's
limitations only when evidence supports the change.

## Consequences

- The performance solver is a measured experiment, not a production solver.
- `lp-solver-performance` must stay independent of external solver libraries
  and external processes.
- Benchmark smoke can run without third-party binaries because the in-project
  solver is always available, but reports still need validation records before
  any conclusion.
- Interior-point and first-order work remains future design work after the
  revised-simplex-style experiment has evidence.
