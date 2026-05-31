package io.github.mundanej.mlp.generators;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class NetworkFlowGeneratorTest {
  @Test
  void generatesDeterministicInstanceForSeed() {
    GeneratedLpInstance first = new NetworkFlowGenerator().threeNode(7L);
    GeneratedLpInstance second = new NetworkFlowGenerator().threeNode(7L);

    assertEquals(first.id(), second.id());
    assertEquals(first.sizeParameters(), second.sizeParameters());
    assertEquals(
        first.fixture().evidence().objectiveValue(), second.fixture().evidence().objectiveValue());
    assertArrayEquals(first.fixture().evidence().primal(), second.fixture().evidence().primal());
  }

  @Test
  void recordsMetadataAndExpectedEvidence() {
    GeneratedLpInstance instance = new NetworkFlowGenerator().threeNode(7L);

    assertEquals("network-flow-3-node-seed-7", instance.id());
    assertEquals(NetworkFlowGenerator.GENERATOR_NAME, instance.generatorName());
    assertEquals(7L, instance.seed());
    assertEquals(3, instance.sizeParameters().get("nodes"));
    assertEquals(3, instance.sizeParameters().get("arcs"));
    assertEquals(ExpectedResultKind.OPTIMAL, instance.fixture().evidence().resultKind());
    assertEquals(1, instance.fixture().problem().stats().rows());
    assertEquals(3, instance.fixture().problem().stats().columns());
    assertEquals(2, instance.fixture().problem().stats().nonzeros());
    assertEquals("relay-balance", instance.fixture().rowNames().getFirst());
    assertEquals("source_middle", instance.fixture().columnNames().getFirst());
  }

  @Test
  void knownEvidenceMatchesCapacityFormula() {
    GeneratedLpInstance instance = new NetworkFlowGenerator().threeNode(7L);
    int sourceToMiddle = instance.sizeParameters().get("sourceToMiddleCapacity");
    int middleToSink = instance.sizeParameters().get("middleToSinkCapacity");
    int sourceToSink = instance.sizeParameters().get("sourceToSinkCapacity");
    double relayFlow = Math.min(sourceToMiddle, middleToSink);
    double directFlow = sourceToSink;

    assertArrayEquals(
        new double[] {relayFlow, relayFlow, directFlow}, instance.fixture().evidence().primal());
    assertEquals(
        relayFlow + directFlow, instance.fixture().evidence().objectiveValue().orElseThrow());
  }

  @Test
  void generatedInstanceRejectsBlankMetadata() {
    GeneratedLpInstance instance = new NetworkFlowGenerator().threeNode(7L);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new GeneratedLpInstance(
                " ",
                instance.generatorName(),
                instance.seed(),
                instance.sizeParameters(),
                instance.fixture()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new GeneratedLpInstance(
                instance.id(), "", instance.seed(), instance.sizeParameters(), instance.fixture()));
  }
}
