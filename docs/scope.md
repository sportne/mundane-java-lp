# Scope

## In scope for G0-G4

- Canonical LP model representation.
- Primitive sparse matrix structures.
- MPS-oriented interchange path.
- Independent result validation.
- Solver adapter SPI.
- Harness CLI and report skeleton.
- Design-control and architecture tests.

## In scope for later gates

- Generated LP families with known primal/dual evidence.
- Command-line adapters for HiGHS, CLP, and GLPK.
- In-process Java adapters where dependencies are acceptable.
- GraalVM native executable and shared-library interfaces.
- First-order LP solver experiments.

## Out of scope initially

- Mixed-integer programming.
- Algebraic modeling language.
- General-purpose commercial-solver replacement claims.
- Automatic solver discovery through classpath scanning or ServiceLoader.
- Reflection-heavy runtime binding.
- Unbounded benchmark claims without recorded evidence.
