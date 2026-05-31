package io.github.mundanej.mlp.generators;

/** Hand-checked fixture outcome used before solver adapters produce evidence. */
public enum ExpectedResultKind {
  /** Fixture has a known optimal primal solution. */
  OPTIMAL,
  /** Fixture is infeasible by inspection. */
  INFEASIBLE,
  /** Fixture is unbounded by inspection. */
  UNBOUNDED
}
