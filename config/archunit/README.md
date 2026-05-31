# Architecture-test configuration

G0 uses source-shape tests in `modules/lp-architecture-tests` instead of an
external ArchUnit dependency. A later gate may replace or supplement these with
ArchUnit if that improves maintainability.

These tests remain part of `qualityGate` alongside Checkstyle, Spotless,
SpotBugs, compiler warnings, and coverage verification.
