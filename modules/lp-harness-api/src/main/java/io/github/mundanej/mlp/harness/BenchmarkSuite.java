package io.github.mundanej.mlp.harness;

import java.util.List;

/** Group of benchmark instances. */
public record BenchmarkSuite(String id, List<BenchmarkInstance> instances) {
    /** Creates a suite. */
    public BenchmarkSuite {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        instances = List.copyOf(instances);
    }
}
