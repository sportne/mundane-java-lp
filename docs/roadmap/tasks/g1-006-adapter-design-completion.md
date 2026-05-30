# G1-006 adapter design completion

## Status

proposed

## Requirement IDs

- REQ-V1-ADAPTER-DESIGN

## References

- `docs/architecture/solver-adapter-contract.md`
- `docs/verification/solver-comparison.md`
- `docs/adr/ADR-0002-command-line-adapters-first.md`

## Target module

- Adapter modules.

## Allowed files

- `docs/architecture/solver-adapter-contract.md`
- `docs/verification/solver-comparison.md`
- Adapter module `README.md` files.
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Build dependency changes.

## Required behavior

- Define adapter responsibilities for HiGHS, CLP, GLPK, OR-Tools, and ojAlgo.
- Define status normalization, version capture, timeout behavior, bounded logs,
  unavailable solver reporting, and work-directory policy.
- Keep third-party dependencies isolated to adapter modules.

## Required tests

- No production tests required; this is a design task.
- Confirm each later adapter task has a direct design reference.

## Required docs

- Update solver adapter contract, solver comparison docs, and adapter READMEs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
