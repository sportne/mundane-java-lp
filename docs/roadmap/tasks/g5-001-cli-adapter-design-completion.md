# G5-001 CLI adapter design completion

## Status

complete

## Requirement IDs

- REQ-0-1-CLI-ADAPTER-DESIGN

## References

- `docs/architecture/solver-adapter-contract.md`
- `docs/verification/solver-comparison.md`
- `docs/adr/ADR-0002-command-line-adapters-first.md`

## Target module

- CLI adapter modules.

## Allowed files

- `docs/architecture/solver-adapter-contract.md`
- `docs/verification/solver-comparison.md`
- CLI adapter module `README.md` files.
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Build dependency changes.

## Required behavior

- Define CLI adapter responsibilities for HiGHS, CLP, and GLPK.
- Define status normalization, version capture, timeout behavior, bounded logs,
  unavailable solver reporting, and work-directory policy.
- Keep CLI process execution isolated to CLI adapter modules.

## Required tests

- No production tests required; this is a design task.
- Confirm each later CLI adapter task has a direct design reference.

## Required docs

- Update solver adapter contract, solver comparison docs, and CLI adapter
  READMEs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
