package io.github.mundanej.mlp.examples.tiny;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.harness.RunOutcome;
import io.github.mundanej.mlp.harness.RunRecord;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class TinyLpExampleTest {
  @TempDir private Path tempDir;

  @Test
  void runsSimpleSolverThroughMpsAndHarnessPath() throws IOException {
    RunRecord record = TinyLpExample.runSimpleMpsSmoke(tempDir);

    assertEquals("tiny-mps-single-bounded-variable", record.instanceId());
    assertEquals("simple", record.solverResult().solverId().name());
    assertEquals(SolverStatus.OPTIMAL, record.solverResult().status());
    assertEquals(RunOutcome.SUCCESS, record.outcome());
    assertEquals(0.0d, record.solverResult().objectiveValue().orElseThrow());
    assertTrue(Files.exists(tempDir.resolve("single-bounded-variable.mps")));
    assertTrue(Files.exists(tempDir.resolve("work")));
  }

  @Test
  void mainPrintsSmokeSummary() throws IOException {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Path outputDirectory = tempDir.resolve("main");
    try {
      System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

      TinyLpExample.main(new String[] {outputDirectory.toString()});
    } finally {
      System.setOut(originalOut);
    }

    String summary = output.toString(StandardCharsets.UTF_8);
    assertTrue(summary.contains("tiny-lp simple MPS smoke"));
    assertTrue(summary.contains("solver=simple"));
    assertTrue(summary.contains("status=OPTIMAL"));
    assertTrue(summary.contains("outcome=SUCCESS"));
    assertTrue(Files.exists(outputDirectory.resolve("single-bounded-variable.mps")));
  }
}
