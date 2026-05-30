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

## Current status

G0 scaffold is present. No non-human-gated implementation task is assumed ready
beyond scaffold cleanup.

## Version 1.0.0 waterfall

Version 1.0.0 is complete when the project has design-complete documentation,
hand-checkable fixtures, a functioning comparison harness for HiGHS, CLP, GLPK,
OR-Tools, and ojAlgo, and two in-project solvers: one simple correctness-first
solver and one performance-oriented solver. Work proceeds in this order.

### Roadmap foundation

- [G0-003 v1 roadmap baseline](tasks/g0-003-v1-roadmap-baseline.md)
- [G0-004 doc placeholder inventory](tasks/g0-004-doc-placeholder-inventory.md)
- [G0-005 doc consolidation pass](tasks/g0-005-doc-consolidation-pass.md)
- [G0-006 Javadoc quality baseline](tasks/g0-006-javadoc-quality-baseline.md)

### Documentation and design

- [G1-001 LP model design completion](tasks/g1-001-lp-model-design-completion.md)
- [G1-002 sparse storage design completion](tasks/g1-002-sparse-storage-design-completion.md)
- [G1-003 MPS format design completion](tasks/g1-003-mps-format-design-completion.md)
- [G1-004 validation design completion](tasks/g1-004-validation-design-completion.md)
- [G1-005 harness design completion](tasks/g1-005-harness-design-completion.md)
- [G1-006 adapter design completion](tasks/g1-006-adapter-design-completion.md)
- [G1-007 simple solver design decision](tasks/g1-007-simple-solver-design-decision.md)
- [G1-008 performance solver design decision](tasks/g1-008-performance-solver-design-decision.md)
- [G1-009 literature notes completion](tasks/g1-009-literature-notes-completion.md)
- [G1-010 doc cleanup review](tasks/g1-010-doc-cleanup-review.md)

### Harness and fixtures

- [G2-001 canonical model fixtures](tasks/g2-001-canonical-model-fixtures.md)
- [G2-002 sparse matrix test coverage](tasks/g2-002-sparse-matrix-test-coverage.md)
- [G2-003 MPS round-trip fixtures](tasks/g2-003-mps-round-trip-fixtures.md)
- [G2-004 validation engine fixtures](tasks/g2-004-validation-engine-fixtures.md)
- [G2-005 generated fixture baseline](tasks/g2-005-generated-fixture-baseline.md)
- [G2-006 testkit simplification pass](tasks/g2-006-testkit-simplification-pass.md)

### Third-party harness

- [G3-001 harness execution model](tasks/g3-001-harness-execution-model.md)
- [G3-002 report output baseline](tasks/g3-002-report-output-baseline.md)
- [G3-003 HiGHS CLI adapter](tasks/g3-003-highs-cli-adapter.md)
- [G3-004 CLP CLI adapter](tasks/g3-004-clp-cli-adapter.md)
- [G3-005 GLPK CLI adapter](tasks/g3-005-glpk-cli-adapter.md)
- [G3-006 OR-Tools Java adapter](tasks/g3-006-ortools-java-adapter.md)
- [G3-007 ojAlgo adapter](tasks/g3-007-ojalgo-adapter.md)
- [G3-008 solver comparison smoke](tasks/g3-008-solver-comparison-smoke.md)
- [G3-009 harness doc cleanup](tasks/g3-009-harness-doc-cleanup.md)

### In-project solvers

- [G4-001 simple solver SPI](tasks/g4-001-simple-solver-spi.md)
- [G4-002 simple solver correctness](tasks/g4-002-simple-solver-correctness.md)
- [G4-003 simple solver MPS path](tasks/g4-003-simple-solver-mps-path.md)
- [G4-004 performance solver SPI](tasks/g4-004-performance-solver-spi.md)
- [G4-005 performance solver core](tasks/g4-005-performance-solver-core.md)
- [G4-006 performance solver correctness](tasks/g4-006-performance-solver-correctness.md)
- [G4-007 performance solver benchmark smoke](tasks/g4-007-performance-solver-benchmark-smoke.md)
- [G4-008 solver code simplification pass](tasks/g4-008-solver-code-simplification-pass.md)

### Release hardening

- [G5-001 native-image smoke](tasks/g5-001-native-image-smoke.md)
- [G5-002 public API Javadoc review](tasks/g5-002-public-api-javadoc-review.md)
- [G5-003 doc final cleanup](tasks/g5-003-doc-final-cleanup.md)
- [G5-004 quality gate hardening](tasks/g5-004-quality-gate-hardening.md)
- [G5-005 v1 release readiness](tasks/g5-005-v1-release-readiness.md)
