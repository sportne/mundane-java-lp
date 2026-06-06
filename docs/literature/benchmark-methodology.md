# Benchmark methodology

## Design notes

- Benchmarks must start as evidence collection, not performance claims. Every
  report needs solver version, options, machine metadata, instance provenance,
  status, residuals, timing, and unavailable-solver handling.
- Performance profiles are useful once there are enough comparable solvers and
  instances. Before that, smoke tests should focus on reproducibility and
  failure classification.
- Public benchmark sets are valuable only with provenance and licensing recorded
  next to the instances. Generated families remain necessary for deterministic,
  hand-scaled, and stress-oriented coverage.
- The benchmark harness should separate correctness acceptance from runtime
  measurements so a fast incorrect result is not counted as a win.

## 0.1.0 Timing Policy

Benchmark lanes run in deterministic suite and solver order. The current 0.1.0
lanes perform no warmup and one measured repetition unless a developer-run lane
emits repeated records for the same mode, suite, instance, and solver.

Report summaries include warmup count, repetition count, min/median/max solve
seconds, failure count, and unavailable count for each mode/suite/instance/solver
group. Comparative timing summaries only include records that pass validation,
have a successful run outcome, and end in a solver status that represents a
solved terminal LP classification: `OPTIMAL`, `FEASIBLE`, `INFEASIBLE`,
`UNBOUNDED`, or `INFEASIBLE_OR_UNBOUNDED`.

Timeouts, memory limits, numerical failures, adapter errors, unknown statuses,
unsupported solver subsets, unavailable solvers, and missing public inputs remain
visible as report records. They are counted in failure or unavailable summary
fields, but their elapsed time does not contribute to min/median/max timing
evidence.

## Roadmap references

- `g7-002-public-benchmark-curation`
- `g7-003-benchmark-evidence-baseline`
- `g7-004-performance-benchmark-suite-baseline`
- `g9-017-performance-evidence-report`
- `g10-005-benchmark-methodology-and-statistics`

## References

- Dolan and More, "Benchmarking Optimization Software with Performance
  Profiles," Mathematical Programming, 2002,
  <https://doi.org/10.1007/s101070100263>.
- Netlib LP test set, <https://www.netlib.org/lp/>.
- Mittelmann optimization benchmark collection,
  <https://plato.asu.edu/bench.html>.
