package io.github.mundanej.mlp.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

final class BenchmarkFixtureGeneratorTest {
  @Test
  void suiteContainsDeterministicExpandedBenchmarkFamilies() {
    BenchmarkFixtureGenerator generator = new BenchmarkFixtureGenerator();

    List<GeneratedLpInstance> first = generator.suite();
    List<GeneratedLpInstance> second = generator.suite();

    assertEquals(6, first.size());
    assertEquals(ids(first), ids(second));
    assertEquals(Set.of(1, 2, 3, 4, 5), familyIds(first));
    assertTrue(ids(first).contains("network-flow-3-node-seed-19"));
    assertTrue(first.stream().allMatch(instance -> instance.fixture().evidence().hasPrimal()));
  }

  @Test
  void individualFamiliesExposeStableShapes() {
    BenchmarkFixtureGenerator generator = new BenchmarkFixtureGenerator();

    assertEquals(2, generator.smallDense().fixture().problem().stats().rows());
    assertEquals(4, generator.sparse().fixture().problem().stats().columns());
    assertEquals(2, generator.equalityHeavy().fixture().problem().rowBounds().size());
    assertEquals(3, generator.degenerate().fixture().problem().stats().rows());
    assertEquals(1, generator.scaled().fixture().problem().stats().rows());
  }

  private static Set<String> ids(final List<GeneratedLpInstance> instances) {
    return instances.stream().map(GeneratedLpInstance::id).collect(Collectors.toSet());
  }

  private static Set<Integer> familyIds(final List<GeneratedLpInstance> instances) {
    return instances.stream()
        .filter(
            instance -> BenchmarkFixtureGenerator.GENERATOR_NAME.equals(instance.generatorName()))
        .map(instance -> instance.sizeParameters().get("family"))
        .collect(Collectors.toSet());
  }
}
