package io.github.mundanej.mlp.examples.tiny;

import io.github.mundanej.mlp.generators.TinyLpGenerator;
import io.github.mundanej.mlp.model.LpProblem;

/** Tiny LP example. */
public final class TinyLpExample {
  private TinyLpExample() {}

  /** Runs the example. */
  public static void main(final String[] args) {
    LpProblem problem = new TinyLpGenerator().singleBoundedVariable();
    System.out.println(problem.name() + " columns=" + problem.stats().columns());
  }
}
