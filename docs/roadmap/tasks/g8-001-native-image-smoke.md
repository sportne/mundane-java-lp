# G8-001 native-image smoke

## Status

complete

## Requirement IDs

- REQ-0-1-NATIVE-SMOKE

## References

- `docs/architecture/native-image.md`
- `docs/verification/native-image-verification.md`
- `docs/adr/ADR-0005-native-image-friendly-core.md`

## Target module

- `modules/lp-native-api`
- `modules/lp-native-tests`
- `examples/native-cli-smoke`

## Allowed files

- `modules/lp-native-api/`
- `modules/lp-native-tests/`
- `examples/native-cli-smoke/`
- Native docs and READMEs.

## Forbidden files

- Solver algorithm redesign.
- Third-party adapter behavior.

## Required behavior

- Make `nativeSmoke` meaningful for 0.1.0.
- Verify native CLI startup and a tiny validated run when native-image is
  available.
- Skip or report unavailable native tooling cleanly when it is absent.

## Required tests

- Native smoke tests for create/run behavior when tooling is present.
- Skip-path tests or task behavior checks for machines without native-image.

## Required docs

- Update native architecture and native verification docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate nativeSmoke --console=plain
```
