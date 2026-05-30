# Security policy

This project reads model files, launches optional external solver binaries, and
may eventually expose native libraries. Treat hostile input as part of the test
matrix.

## Reporting issues

Please report security issues privately to the maintainer before public issue
creation.

## Security-sensitive areas

- MPS/LP parsing.
- Temporary working directories and solver log handling.
- External process execution in CLI adapter modules.
- Native executable and shared-library boundaries.
- Benchmark artifact ingestion.

## Default posture

- Core modules must not execute external processes.
- CLI adapter modules must treat executable paths as explicit configuration.
- Temporary directories must be created with narrow scope and cleaned where
  practical.
- Parser diagnostics should be bounded and avoid echoing untrusted large input.
