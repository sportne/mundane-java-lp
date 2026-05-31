package io.github.mundanej.mlp.generators;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.SplittableRandom;

/** Deterministic generator for small network-flow-like LP instances. */
public final class NetworkFlowGenerator {
  /** Generator name recorded in generated instance metadata. */
  public static final String GENERATOR_NAME = "network-flow-3-node";

  /**
   * Generates a deterministic three-node network-flow-like LP.
   *
   * @param seed deterministic seed
   */
  public GeneratedLpInstance threeNode(final long seed) {
    SplittableRandom random = new SplittableRandom(seed);
    int sourceToMiddleCapacity = 1 + random.nextInt(5);
    int middleToSinkCapacity = 1 + random.nextInt(5);
    int sourceToSinkCapacity = 1 + random.nextInt(5);
    double relayFlow = Math.min(sourceToMiddleCapacity, middleToSinkCapacity);
    double directFlow = sourceToSinkCapacity;
    double objectiveValue = relayFlow + directFlow;
    CanonicalLpFixture fixture =
        new CanonicalLpFixture(
            problem(seed, sourceToMiddleCapacity, middleToSinkCapacity, sourceToSinkCapacity),
            matrix(),
            List.of("relay-balance"),
            List.of("source_middle", "middle_sink", "source_sink"),
            new LpFixtureEvidence(
                ExpectedResultKind.OPTIMAL,
                OptionalDouble.of(objectiveValue),
                new double[] {relayFlow, relayFlow, directFlow}));
    return new GeneratedLpInstance(
        "network-flow-3-node-seed-" + seed,
        GENERATOR_NAME,
        seed,
        Map.of(
            "nodes", 3,
            "arcs", 3,
            "sourceToMiddleCapacity", sourceToMiddleCapacity,
            "middleToSinkCapacity", middleToSinkCapacity,
            "sourceToSinkCapacity", sourceToSinkCapacity),
        fixture);
  }

  private static LpProblem problem(
      final long seed,
      final int sourceToMiddleCapacity,
      final int middleToSinkCapacity,
      final int sourceToSinkCapacity) {
    return new LpProblem(
        "generated-network-flow-3-node-" + seed,
        new LpObjective(ObjectiveSense.MAXIMIZE, 0.0d, new double[] {1.0d, 0.0d, 1.0d}),
        List.of(
            new LpVariableBounds(0.0d, sourceToMiddleCapacity),
            new LpVariableBounds(0.0d, middleToSinkCapacity),
            new LpVariableBounds(0.0d, sourceToSinkCapacity)),
        List.of(new LpRowBounds(0.0d, 0.0d)),
        new LpProblemStats(1, 3, 2));
  }

  private static CsrMatrix matrix() {
    return new CsrMatrix(1, 3, new double[] {1.0d, -1.0d}, new int[] {0, 1}, new int[] {0, 2});
  }
}
