# Documentation placeholder inventory

G0-004 started the record of placeholder, deferred, future, stale, and
duplicate documentation language so cleanup tasks can remove or preserve it
deliberately instead of incidentally. Release-002 refreshed this inventory for
the 0.1.0 release-hardening baseline.

## Search method

The release-002 inventory was refreshed from this search over Markdown
documentation:

```bash
rg -n -i '\b(TODO|TBD|placeholder|deferred|future|later|stale|duplicate|G0 scaffold|G0 design-control|next product work|not implemented|not yet|will be|planned|roadmap work)\b' README.md AGENT.md docs modules examples --glob '*.md' -g '!docs/roadmap/tasks/*.md'
```

Task files are trace records and are intentionally excluded from consolidation
or removal decisions unless a task explicitly allows task-file maintenance.

## Inventory

| Area | Current language | Classification | Owner |
| --- | --- | --- | --- |
| `README.md`, `docs/charter.md`, `docs/scope.md` | Former G0/G1 and pre-solver phase language was replaced with the current 0.1.0 release-hardening posture. | Cleaned | `release-002-doc-final-cleanup` owns this release-facing wording. |
| `docs/architecture/module-boundaries.md`, `docs/architecture/native-image.md`, `modules/lp-native-api/README.md` | Former placeholder/planned wording was replaced with current 0.1.0 module surfaces and explicit C ABI exclusion. | Cleaned | `release-002-doc-final-cleanup` reconciled landed G8/G9 behavior without adding shared-library scope. |
| `docs/verification/*.md` | Stale `not yet` and future-lane wording was replaced with current evidence status, reserve lanes, or explicit 0.1.0 exclusions. | Cleaned | `release-002-doc-final-cleanup` aligned the verification docs without changing any Gradle lane. |
| `docs/standards-baseline.md` | LP text format is outside the 0.1.0 MPS-first interchange surface. | Keep | This is an intentional 0.1.0 limitation, not a stale placeholder. |
| `docs/architecture/sparse-matrix-contract.md`, `docs/verification/generated-instance-families.md`, `docs/literature/sparse-linear-algebra.md`, `docs/architecture/solver-adapter-contract.md` | Remaining `duplicate` hits describe sparse-coordinate or input-validation semantics. | Keep | These are literal domain terms and should remain. |
| `docs/adr/*.md`, `docs/literature/*methods.md` | Remaining forward-looking hits describe accepted design history or research paths outside 0.1.0, such as first-order and interior-point work. | Keep | ADR history and non-0.1.0 algorithm directions remain useful context. |
| `docs/roadmap/roadmap-index.md` | The index still defines future/proposed task language and links to this inventory. | Keep | Roadmap status vocabulary is intentional. |
| `docs/roadmap/tasks/*.md` | Proposed tasks contain future, later, stale, placeholder, and cleanup wording. | Keep | Task files are exempt trace records and intentionally describe open work. |

## Consolidation candidates

- No obvious duplicate markdown file should be consolidated as part of
  release-002.
- Release-003 may harden quality-gate documentation and task wiring, but that
  is outside this documentation-only cleanup.

## Intentional remaining terms

After release-002, placeholder and forward-looking terms are expected to remain
only in task files, this inventory, roadmap status vocabulary, ADR history,
literal sparse/validation terminology, and explicit out-of-0.1.0 algorithm or
native-library scope. New occurrences outside these categories should identify
an owning roadmap task or be removed during documentation cleanup.
