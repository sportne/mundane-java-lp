# Performance profiling baseline

G9-011 records the first profiling baseline for the in-project performance
solver before optimization work begins. These notes are evidence for choosing
optimization targets, not performance claims.

## Command

Baseline profiling used the normal benchmark smoke lane:

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```

The full task gate also ran `solverComparisonSmoke` and `nativeSmoke`, but the
profiling data below comes from `benchmarkSmoke` reports under
`modules/lp-harness-cli/build/reports/benchmark-smoke/`.

## Environment

Sample run metadata from the benchmark report:

- OS: Linux
- architecture: amd64
- Java: 21.0.11
- processors: 32
- solver options: 1 thread, 60 second timeout
- tolerance: `STANDARD`
- peak memory: `not-measured`

## Instance Set

- `network-flow-3-node-seed-7`: generated deterministic network-flow-like
  fixture from `NetworkFlowGenerator`.
- `netlib-afiro`, `netlib-adlittle`, and `netlib-scorpion`: curated public
  manifest candidates recorded as missing local public inputs in this run.

## Observations

The generated fixture produced an accepted performance-solver record. One
sample report recorded:

- performance solver status: `OPTIMAL`
- objective: `6.0`
- solve seconds: `0.094670088`
- validation seconds: `0.036220264`
- total seconds: `0.276658218`
- parse/export: `not-measured`

The generated evidence adapter record exists as a fixture sanity record and is
not a solver-performance comparison. Public candidates did not run solver work
because local MPS files were absent.

## Bottlenecks

The sample is too small for ranking algorithmic costs, but it still identifies
the first avoidable hot-path shape:

- `RevisedSimplexCore` converts sparse input into dense row arrays before
  building constraints.
- Finite variable upper bounds allocate one dense coefficient vector each.
- Constraint normalization clones coefficient arrays before tableau assembly.
- Tableau assembly copies dense coefficients again.

These allocations are acceptable for the first correctness baseline, but they
are the wrong direction for the native-friendly sparse-storage design.

## First Optimization Target

G9-012 should target sparse-to-constraint setup and tableau input construction:
avoid repeated dense coefficient-vector allocation where the caller can reuse
buffers or where CSR row slices can be copied once into the tableau. The goal is
lower allocation pressure with unchanged solver results, not a public runtime
claim.

## Iteration 1 Result

G9-012 added a low-allocation CSR `copyRowInto(row, output)` API and updated the
performance solver to reuse a single dense row buffer while translating CSR rows
into linear constraints. Variable upper-bound constraints now build owned unit
vectors without the extra normalization clone.

The change removes the full `rows x columns` dense matrix allocation from solver
setup. Constraint objects still own defensive coefficient arrays, and tableau
assembly still copies coefficients into tableau storage. Those remaining copies
are intentional until a later iteration can prove a narrower tableau input
contract.

Focused validation after the change used:

```bash
./gradlew :modules:lp-sparse:test :modules:lp-solver-performance:test benchmarkSmoke --console=plain
```

One post-change sample report recorded:

- performance solver status: `OPTIMAL`
- objective: `6.0`
- solve seconds: `0.036619611`
- validation seconds: `0.012539286`
- total seconds: `0.113334826`
- parse/export: `not-measured`

These numbers are a smoke-run snapshot only. The evidence supports the narrower
allocation change and preserves the no-public-performance-claim policy.
