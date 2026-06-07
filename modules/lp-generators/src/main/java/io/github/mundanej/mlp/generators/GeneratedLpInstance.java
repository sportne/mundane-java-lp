package io.github.mundanej.mlp.generators;

import java.util.Map;
import java.util.Objects;

/**
 * Generated LP instance with provenance metadata and canonical fixture data.
 *
 * @param id stable instance identifier
 * @param generatorName generator name
 * @param seed deterministic seed
 * @param sizeParameters deterministic size parameters; copied into an immutable map
 * @param fixture canonical LP fixture
 */
public record GeneratedLpInstance(
    String id,
    String generatorName,
    long seed,
    Map<String, Integer> sizeParameters,
    CanonicalLpFixture fixture) {
  /**
   * Creates generated LP instance metadata.
   *
   * @param id stable instance identifier
   * @param generatorName generator name
   * @param seed deterministic seed
   * @param sizeParameters deterministic size parameters
   * @param fixture canonical LP fixture
   */
  public GeneratedLpInstance {
    id = requireName(id, "id");
    generatorName = requireName(generatorName, "generatorName");
    sizeParameters = Map.copyOf(Objects.requireNonNull(sizeParameters, "sizeParameters"));
    Objects.requireNonNull(fixture, "fixture");
  }

  private static String requireName(final String value, final String label) {
    Objects.requireNonNull(value, label);
    if (value.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    return value;
  }
}
