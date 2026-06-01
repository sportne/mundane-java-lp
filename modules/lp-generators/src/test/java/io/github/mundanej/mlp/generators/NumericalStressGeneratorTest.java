package io.github.mundanej.mlp.generators;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

final class NumericalStressGeneratorTest {
  @Test
  void suiteIsDeterministic() {
    NumericalStressGenerator generator = new NumericalStressGenerator();

    List<GeneratedLpInstance> first = generator.suite();
    List<GeneratedLpInstance> second = generator.suite();

    assertEquals(names(first), names(second));
    for (int index = 0; index < first.size(); index++) {
      assertEquals(first.get(index).seed(), second.get(index).seed());
      assertEquals(first.get(index).sizeParameters(), second.get(index).sizeParameters());
      assertArrayEquals(
          first.get(index).fixture().matrix().values(),
          second.get(index).fixture().matrix().values());
      assertArrayEquals(
          first.get(index).fixture().evidence().primal(),
          second.get(index).fixture().evidence().primal());
    }
  }

  @Test
  void recordsExpectedStressFamiliesAndEvidence() {
    List<GeneratedLpInstance> suite = new NumericalStressGenerator().suite();

    assertEquals(
        List.of(
            "stress-scaling",
            "stress-degeneracy",
            "stress-tight-bounds",
            "stress-ill-conditioned-ranged"),
        names(suite));
    assertEquals(ExpectedResultKind.OPTIMAL, suite.get(0).fixture().evidence().resultKind());
    assertEquals(1.0d, suite.get(0).fixture().evidence().objectiveValue().orElseThrow());
    assertEquals(1.0e6d, suite.get(0).fixture().evidence().primal()[0]);
    assertEquals(ExpectedResultKind.OPTIMAL, suite.get(1).fixture().evidence().resultKind());
    assertEquals(ExpectedResultKind.OPTIMAL, suite.get(2).fixture().evidence().resultKind());
    assertEquals(ExpectedResultKind.OPTIMAL, suite.get(3).fixture().evidence().resultKind());
    assertEquals(
        1.0d / (1.0d + 1.0e-10d), suite.get(3).fixture().evidence().objectiveValue().orElseThrow());
  }

  private static List<String> names(final List<GeneratedLpInstance> instances) {
    return instances.stream().map(instance -> instance.fixture().problem().name()).toList();
  }
}
