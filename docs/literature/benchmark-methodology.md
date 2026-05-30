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

## Roadmap references

- `g7-002-public-benchmark-curation`
- `g7-003-benchmark-evidence-baseline`
- `g7-004-performance-benchmark-suite-baseline`
- `g9-017-performance-evidence-report`

## References

- Dolan and More, "Benchmarking Optimization Software with Performance
  Profiles," Mathematical Programming, 2002,
  <https://doi.org/10.1007/s101070100263>.
- Netlib LP test set, <https://www.netlib.org/lp/>.
- Mittelmann optimization benchmark collection,
  <https://plato.asu.edu/bench.html>.
