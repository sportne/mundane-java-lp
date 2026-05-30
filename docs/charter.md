# Project charter

`mundane-java-lp` exists to make linear-programming solver behavior explicit,
repeatable, and testable in a Java/GraalVM setting.

The first product is not a solver. The first product is a correctness and
benchmark harness that compares established solvers and records enough evidence
to guide a future Java-native implementation.

## Audience

- Maintainers exploring a Java-native LP solver.
- Users comparing LP solver behavior on large sparse models.
- Coding agents implementing tightly scoped roadmap tasks.

## Operating principles

- Harness before solver.
- Validation before performance claims.
- Recorded evidence before conclusions.
- Native Image friendliness by architecture, not afterthought.
- Module boundaries enforced mechanically where practical.
