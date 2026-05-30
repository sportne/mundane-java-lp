# Documentation placeholder inventory

G0-004 records known placeholder, deferred, future, stale, and duplicate
documentation language so later tasks can remove it deliberately instead of
incidentally.

## Search method

The inventory was built from this search over Markdown documentation:

```bash
rg -n -i '\b(TODO|TBD|placeholder|deferred|future|later|stale|duplicate|G0 scaffold|not implemented|not yet|will be|planned)\b' README.md AGENT.md docs modules examples --glob '*.md'
```

Task files are trace records and are intentionally excluded from consolidation
or removal decisions unless a task explicitly allows task-file maintenance.

## Inventory

| Area | Current language | Classification | Owner |
| --- | --- | --- | --- |
| `docs/literature/*.md` | Topic files contain `G0 placeholder` and future-note structure. | Fill | `g0-007-literature-notes-completion` replaces placeholders with concise cited notes. |
| `docs/literature/literature-index.md` | Describes topic placeholders and later citation work. | Fill | `g0-007-literature-notes-completion` updates the index after topic notes exist. |
| `modules/*/README.md` | Module READMEs formerly said `G0 scaffold module`. | Fill | `g0-005-doc-consolidation-pass` replaced generic scaffold text with module-purpose summaries. Later owners are `g1-001`, `g2-001`, `g2-003`, `g3-001`, `g4-001`, `g5-001`, `g6-001`, `g8-001`, and G9 solver tasks by module area. |
| `README.md` | Current phase says G0; Java-native solvers are described as roadmap work; OR-Tools/ojAlgo status was formerly duplicated here. | Merge | `g0-005-doc-consolidation-pass` removed duplicate adapter placeholder wording; G9 tasks own Java-native solver progress; `g6-004-java-adapter-comparison-smoke` and release cleanup tasks update phase and adapter status later. |
| `docs/verification/solver-comparison.md` | OR-Tools and ojAlgo are listed as placeholders. | Merge | `g0-005-doc-consolidation-pass` keeps adapter-status wording here; `g6-004-java-adapter-comparison-smoke` updates Java adapter behavior later. |
| `docs/verification/native-image-verification.md` | Native lane says real native builds are deferred and lists future checks. | Fill | `g8-001-native-image-smoke` makes the native smoke meaningful; `g8-002-allocation-and-native-friendliness-pass` updates friendliness checks. |
| `docs/verification/public-benchmark-sources.md` | Public benchmark ingestion is described as future work. | Fill | `g7-002-public-benchmark-curation` owns benchmark source criteria and provenance. |
| `docs/verification/generated-instance-families.md` | Generated families are planned but not implemented. | Fill | `g7-001-generated-fixture-baseline` owns deterministic generated families. |
| `docs/verification/verification-strategy.md` | Manual future lanes are named without implementation. | Keep | The list is intentional lane planning until `release-003-quality-gate-hardening` reconciles documented lanes with implemented commands. |
| `docs/architecture/module-boundaries.md` | `lp-io-lp` and native API are marked placeholder/planned. | Fill | `g2-003-mps-format-design-completion`, `g8-001-native-image-smoke`, and release cleanup tasks update boundaries as behavior lands. |
| `docs/architecture/harness-architecture.md` | Future gates fill in harness execution behavior. | Fill | `g4-001-harness-design-completion` and `g4-002-harness-execution-model` own this language. |
| `docs/architecture/lp-model-contract.md` | Notes that G0 model classes do not yet embed sparse storage. | Fill | `g1-001-lp-model-design-completion` owns canonical model invariants. |
| `docs/architecture/architecture-rule-catalog.md` | Several rules list future tests. | Fill | `g0-006-javadoc-quality-baseline`, gate-specific architecture tests, and `release-003-quality-gate-hardening` own enforcement. |
| `docs/architecture/native-image.md` | Planned native targets are listed. | Fill | `g8-001-native-image-smoke` and release hardening update target evidence. |
| `docs/scope.md`, `docs/charter.md`, ADRs | They describe later gates or future Java-native implementation. | Keep | This is strategic scope language, not stale placeholder text. |
| `docs/standards-baseline.md` | LP text format is deferred until MPS behavior is tested. | Keep | The deferral records the current standards order and should remain until a future LP-format task exists. |
| `docs/roadmap/tasks/*.md` | Proposed tasks contain future, later, stale, placeholder, and cleanup wording. | Keep | Task files are exempt trace records and intentionally describe future work. |

## Consolidation candidates

- Reconcile verification lane lists across `docs/verification/*.md` during
  release hardening after the commands exist.
- Revisit module README purpose lines during gate-specific design tasks when a
  module's public behavior becomes more precise than its G0 purpose statement.

## Intentional remaining terms

After G0-004, placeholder and future-oriented terms are expected to remain in
task files, this inventory, the roadmap index link to this inventory, strategic
scope documents, and documents that are explicitly owned by later tasks above.
Any new occurrence outside these categories should either identify an owning
roadmap task or be removed during G0-005/G0-008 cleanup.
