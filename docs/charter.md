# Project charter

`mundane-java-lp` exists to make linear-programming solver behavior explicit,
repeatable, and testable in a Java/GraalVM setting.

The 0.1.0 product is a correctness and benchmark harness that compares
established solvers, exercises narrow in-project solver experiments, and
records evidence before making correctness or performance claims.

## Audience

- Maintainers exploring Java-native LP solver designs.
- Users comparing LP solver behavior on large sparse models.
- Coding agents implementing tightly scoped roadmap tasks.

## Operating principles

- Harness before solver.
- Validation before performance claims.
- Recorded evidence before conclusions.
- Native Image friendliness by architecture, not afterthought.
- Module boundaries enforced mechanically where practical.
