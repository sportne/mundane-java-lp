package io.github.mundanej.mlp.examples.network;

import io.github.mundanej.mlp.generators.GeneratedLpInstance;
import io.github.mundanej.mlp.generators.NetworkFlowGenerator;

/** Network-flow generator example. */
public final class GeneratedNetworkFlowExample {
    private GeneratedNetworkFlowExample() {
    }

    /** Runs the deterministic generated fixture example. */
    public static void main(final String[] args) {
        long seed = args.length == 0 ? 7L : Long.parseLong(args[0]);
        GeneratedLpInstance instance = new NetworkFlowGenerator().threeNode(seed);
        System.out.println("id=" + instance.id());
        System.out.println("generator=" + instance.generatorName());
        System.out.println("seed=" + instance.seed());
        System.out.println("variables=" + instance.fixture().problem().stats().columns());
        System.out.println("rows=" + instance.fixture().problem().stats().rows());
        System.out.println("objective=" + instance.fixture().evidence().objectiveValue().orElseThrow());
    }
}
