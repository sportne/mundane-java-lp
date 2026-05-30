# G0-002 restore acceptance gate

## Status

complete

## Requirement IDs

- REQ-G0-ACCEPTANCE-GATE

## Target modules

- Root build logic.
- `lp-sparse`.
- `lp-generators`.

## Required behavior

Restore the G0 acceptance gate after scaffold drift:

- `validateDesignControlPack` must be compatible with Gradle configuration
  cache.
- CSC multiplication tests must assert the value represented by the fixture.
- Javadoc for tiny LP fixtures must not emit raw inequality warnings.

## Forbidden files

- `LICENSE`

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
