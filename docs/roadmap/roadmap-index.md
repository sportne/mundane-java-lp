# Roadmap index

## Gates

- **G0**: design-control scaffold and harness foundation.
- **G1**: canonical LP model and tiny fixtures.
- **G2**: sparse matrix storage and MPS read/write.
- **G3**: validation engine.
- **G4**: harness SPI and CLI execution model.
- **G5**: external CLI solver adapters.
- **G6**: Java library solver adapters.
- **G7**: generated LP families.
- **G8**: GraalVM native executable/shared-library smoke.
- **G9**: first in-project solver experiment.

## How gates and tasks relate

Gates are capability milestones. Task file prefixes match the gate they
advance: `g3-*` tasks belong to G3 validation work, `g9-*` tasks belong to G9
in-project solver work, and so on. Release-hardening tasks use the `release-*`
prefix because they happen after the G0-G9 capability gates. Task files are
trace records for completed and future work; they are exempt from markdown
consolidation unless a task explicitly allows task-file maintenance.

## Current status

G0 through G5 are complete. The next open gate is G6 Java library solver
adapters, followed by generated benchmark families, native-image smoke, in-project
solver experiments, and release hardening.

Task status labels in this index:

- **Complete**: implemented and locally/CI validated.
- **Proposed**: planned but not yet implemented.

The current placeholder and stale-language inventory is tracked in
[documentation-placeholder-inventory.md](documentation-placeholder-inventory.md).

## Version 0.1.0 waterfall

Version 0.1.0 is complete when the project has design-complete documentation,
hand-checkable fixtures, a functioning comparison harness for HiGHS, CLP, GLPK,
OR-Tools, and ojAlgo, and two in-project solvers: one simple correctness-first
solver and one performance-oriented solver. The performance solver must have a
credible but modest evidence trail from iterative profiling, optimization,
correctness, and simplification passes. Work proceeds in gate order.

### G0: design-control scaffold and harness foundation

- [G0-001 scaffold](tasks/g0-001-scaffold.md) - **Complete**
- [G0-002 restore acceptance gate](tasks/g0-002-restore-acceptance-gate.md) - **Complete**
- [G0-003 roadmap baseline](tasks/g0-003-roadmap-baseline.md) - **Complete**
- [G0-004 doc placeholder inventory](tasks/g0-004-doc-placeholder-inventory.md) - **Complete**
- [G0-005 doc consolidation pass](tasks/g0-005-doc-consolidation-pass.md) - **Complete**
- [G0-006 Javadoc quality baseline](tasks/g0-006-javadoc-quality-baseline.md) - **Complete**
- [G0-007 literature notes completion](tasks/g0-007-literature-notes-completion.md) - **Complete**
- [G0-008 doc cleanup review](tasks/g0-008-doc-cleanup-review.md) - **Complete**

### G1: canonical LP model and tiny fixtures

- [G1-001 LP model design completion](tasks/g1-001-lp-model-design-completion.md) - **Complete**
- [G1-002 canonical model fixtures](tasks/g1-002-canonical-model-fixtures.md) - **Complete**

### G2: sparse matrix storage and MPS read/write

- [G2-001 sparse storage design completion](tasks/g2-001-sparse-storage-design-completion.md) - **Complete**
- [G2-002 sparse matrix test coverage](tasks/g2-002-sparse-matrix-test-coverage.md) - **Complete**
- [G2-003 MPS format design completion](tasks/g2-003-mps-format-design-completion.md) - **Complete**
- [G2-004 MPS round-trip fixtures](tasks/g2-004-mps-round-trip-fixtures.md) - **Complete**

### G3: validation engine

- [G3-001 validation design completion](tasks/g3-001-validation-design-completion.md) - **Complete**
- [G3-002 validation engine fixtures](tasks/g3-002-validation-engine-fixtures.md) - **Complete**

### G4: harness SPI and CLI execution model

- [G4-001 harness design completion](tasks/g4-001-harness-design-completion.md) - **Complete**
- [G4-002 harness execution model](tasks/g4-002-harness-execution-model.md) - **Complete**
- [G4-003 report output baseline](tasks/g4-003-report-output-baseline.md) - **Complete**
- [G4-004 testkit simplification pass](tasks/g4-004-testkit-simplification-pass.md) - **Complete**

### G5: external CLI solver adapters

- [G5-001 CLI adapter design completion](tasks/g5-001-cli-adapter-design-completion.md) - **Complete**
- [G5-002 HiGHS CLI adapter](tasks/g5-002-highs-cli-adapter.md) - **Complete**
- [G5-003 CLP CLI adapter](tasks/g5-003-clp-cli-adapter.md) - **Complete**
- [G5-004 GLPK CLI adapter](tasks/g5-004-glpk-cli-adapter.md) - **Complete**
- [G5-005 CLI solver comparison smoke](tasks/g5-005-cli-solver-comparison-smoke.md) - **Complete**
- [G5-006 CLI harness doc cleanup](tasks/g5-006-cli-harness-doc-cleanup.md) - **Complete**

### G6: Java library solver adapters

- [G6-001 Java adapter design completion](tasks/g6-001-java-adapter-design-completion.md) - **Proposed**
- [G6-002 OR-Tools Java adapter](tasks/g6-002-ortools-java-adapter.md) - **Proposed**
- [G6-003 ojAlgo adapter](tasks/g6-003-ojalgo-adapter.md) - **Proposed**
- [G6-004 Java adapter comparison smoke](tasks/g6-004-java-adapter-comparison-smoke.md) - **Proposed**

### G7: generated LP families

- [G7-001 generated fixture baseline](tasks/g7-001-generated-fixture-baseline.md) - **Proposed**
- [G7-002 public benchmark curation](tasks/g7-002-public-benchmark-curation.md) - **Proposed**
- [G7-003 benchmark evidence baseline](tasks/g7-003-benchmark-evidence-baseline.md) - **Proposed**
- [G7-004 performance benchmark suite baseline](tasks/g7-004-performance-benchmark-suite-baseline.md) - **Proposed**

### G8: GraalVM native executable/shared-library smoke

- [G8-001 native-image smoke](tasks/g8-001-native-image-smoke.md) - **Proposed**
- [G8-002 allocation and native friendliness pass](tasks/g8-002-allocation-and-native-friendliness-pass.md) - **Proposed**

### G9: first in-project solver experiment

- [G9-001 simple solver design decision](tasks/g9-001-simple-solver-design-decision.md) - **Complete**
- [G9-002 simple solver SPI](tasks/g9-002-simple-solver-spi.md) - **Complete**
- [G9-003 simple solver correctness](tasks/g9-003-simple-solver-correctness.md) - **Complete**
- [G9-004 simple solver MPS path](tasks/g9-004-simple-solver-mps-path.md) - **Complete**
- [G9-005 performance solver design decision](tasks/g9-005-performance-solver-design-decision.md) - **Complete**
- [G9-006 performance solver SPI](tasks/g9-006-performance-solver-spi.md) - **Complete**
- [G9-007 performance solver core](tasks/g9-007-performance-solver-core.md) - **Complete**
- [G9-008 performance solver correctness](tasks/g9-008-performance-solver-correctness.md) - **Proposed**
- [G9-009 performance solver benchmark smoke](tasks/g9-009-performance-solver-benchmark-smoke.md) - **Proposed**
- [G9-010 performance measurement instrumentation](tasks/g9-010-performance-measurement-instrumentation.md) - **Proposed**
- [G9-011 performance solver profiling baseline](tasks/g9-011-performance-solver-profiling-baseline.md) - **Proposed**
- [G9-012 performance iteration 1 sparse hot path](tasks/g9-012-performance-iteration-1-sparse-hot-path.md) - **Proposed**
- [G9-013 iteration 1 correctness and simplification](tasks/g9-013-iteration-1-correctness-and-simplification.md) - **Proposed**
- [G9-014 performance iteration 2 algorithmic bottleneck](tasks/g9-014-performance-iteration-2-algorithmic-bottleneck.md) - **Proposed**
- [G9-015 numerical robustness stress suite](tasks/g9-015-numerical-robustness-stress-suite.md) - **Proposed**
- [G9-016 performance iteration 3 robustness and scaling](tasks/g9-016-performance-iteration-3-robustness-and-scaling.md) - **Proposed**
- [G9-017 performance evidence report](tasks/g9-017-performance-evidence-report.md) - **Proposed**
- [G9-018 performance code simplification pass](tasks/g9-018-performance-code-simplification-pass.md) - **Proposed**
- [G9-019 performance readiness review](tasks/g9-019-performance-readiness-review.md) - **Proposed**

### Release hardening

- [release-001 public API Javadoc review](tasks/release-001-public-api-javadoc-review.md) - **Proposed**
- [release-002 doc final cleanup](tasks/release-002-doc-final-cleanup.md) - **Proposed**
- [release-003 quality gate hardening](tasks/release-003-quality-gate-hardening.md) - **Proposed**
- [release-004 0.1.0 release readiness](tasks/release-004-0-1-0-release-readiness.md) - **Proposed**
