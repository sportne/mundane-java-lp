package io.github.mundanej.mlp.examples.network;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class GeneratedNetworkFlowExampleTest {
  @Test
  void printsGeneratedInstanceMetadata() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    try {
      System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
      GeneratedNetworkFlowExample.main(new String[] {"7"});
    } finally {
      System.setOut(originalOut);
    }

    String text = output.toString(StandardCharsets.UTF_8);
    assertTrue(text.contains("id=network-flow-3-node-seed-7"));
    assertTrue(text.contains("generator=network-flow-3-node"));
    assertTrue(text.contains("variables=3"));
    assertTrue(text.contains("rows=1"));
    assertTrue(text.contains("objective="));
  }
}
