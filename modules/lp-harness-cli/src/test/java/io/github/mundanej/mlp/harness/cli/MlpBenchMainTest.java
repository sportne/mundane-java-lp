package io.github.mundanej.mlp.harness.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.harness.RunOutcome;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MlpBenchMainTest {
  @TempDir private Path tempDir;

  @Test
  void helpDoesNotThrow() {
    assertDoesNotThrow(() -> MlpBenchMain.main(new String[] {"--help"}));
  }

  @Test
  void defaultBenchmarkSmokeDoesNotThrow() {
    assertDoesNotThrow(() -> MlpBenchMain.main(new String[] {tempDir.toString()}));
  }

  @Test
  void benchmarkSmokeWritesReportsAndMissingPublicInputRecord() throws IOException {
    MlpBenchMain.BenchmarkSmokeResult result = MlpBenchMain.runBenchmarkSmoke(tempDir);

    assertEquals(5, result.records().size());
    assertTrue(
        result.records().stream().allMatch(record -> "benchmark-smoke".equals(record.runMode())));
    assertEquals(RunOutcome.SUCCESS, result.records().getFirst().outcome());
    assertEquals("performance", result.records().getFirst().solverResult().solverId().name());
    assertEquals(RunOutcome.SUCCESS, result.records().get(1).outcome());
    assertEquals(RunOutcome.SOLVER_UNAVAILABLE, result.records().get(2).outcome());
    assertTrue(Files.readString(result.markdownPath()).contains("benchmark-smoke-generated"));
    assertTrue(Files.readString(result.jsonPath()).contains("\"instance\":\"netlib-afiro\""));
    assertTrue(Files.readString(result.csvPath()).contains("performance"));
    assertTrue(Files.readString(result.csvPath()).contains("missing local public benchmark file"));
  }

  @Test
  void benchmarkSmokeLoadsPresentPublicMpsFile() throws IOException {
    Path repoRoot = tempDir.resolve("repo");
    Path manifest = repoRoot.resolve("instances/public/manifest.example.json");
    Path publicFile = repoRoot.resolve("instances/public/netlib/afiro.mps");
    Files.createDirectories(publicFile.getParent());
    Files.writeString(
        publicFile,
        """
                NAME          AFIRO
                ROWS
                 N  COST
                 L  R1
                COLUMNS
                    X1        COST      1.0       R1        1.0
                RHS
                    RHS1      R1        5.0
                BOUNDS
                 LO BND1      X1        0.0
                 UP BND1      X1        5.0
                ENDATA
                """);
    Files.writeString(
        manifest,
        """
                {
                  "schemaVersion": 1,
                  "sourceSet": "test",
                  "instances": [
                    {
                      "id": "netlib-afiro",
                      "family": "Netlib LP",
                      "upstreamUrl": "https://www.netlib.org/lp/data/afiro",
                      "licenseOrTerms": "test",
                      "downloadDate": "2026-05-31",
                      "sha256": "test",
                      "format": "MPS",
                      "localPath": "instances/public/netlib/afiro.mps",
                      "normalization": "test",
                      "status": "approved-local"
                    }
                  ]
                }
                """);

    MlpBenchMain.BenchmarkSmokeResult result =
        MlpBenchMain.runBenchmarkSmoke(tempDir.resolve("reports"), manifest);

    assertEquals(3, result.records().size());
    assertEquals(RunOutcome.SUCCESS, result.records().getFirst().outcome());
    assertEquals(RunOutcome.SUCCESS, result.records().get(1).outcome());
    assertEquals(RunOutcome.SOLVER_UNAVAILABLE, result.records().get(2).outcome());
    assertTrue(Files.readString(result.csvPath()).contains("public benchmark loaded"));
  }

  @Test
  void benchmarkSmokeSkipsRejectedAndNonMpsPublicCandidates() throws IOException {
    Path manifest = tempDir.resolve("manifest.json");
    Files.writeString(
        manifest,
        """
                {
                  "instances": [
                    {
                      "id": "rejected",
                      "format": "MPS",
                      "localPath": "ignored.mps",
                      "status": "rejected"
                    },
                    {
                      "id": "not-mps",
                      "format": "LP",
                      "localPath": "ignored.lp",
                      "status": "approved-local"
                    }
                  ]
                }
                """);

    MlpBenchMain.BenchmarkSmokeResult result =
        MlpBenchMain.runBenchmarkSmoke(tempDir.resolve("skip-reports"), manifest);

    assertEquals(2, result.records().size());
    assertEquals(RunOutcome.SUCCESS, result.records().getFirst().outcome());
  }

  @Test
  void benchmarkSmokeRecordsPublicLoadFailure() throws IOException {
    Path manifest = tempDir.resolve("bad-public-manifest.json");
    Path malformedPublicFile = tempDir.resolve("bad-public.mps");
    Files.writeString(
        malformedPublicFile,
        """
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ nope
                RHS
                ENDATA
                """);
    Files.writeString(
        manifest,
        """
                {
                  "instances": [
                    {
                      "id": "bad-public",
                      "format": "MPS",
                      "localPath": "%s",
                      "status": "approved-local"
                    }
                  ]
                }
                """
            .formatted(malformedPublicFile));

    MlpBenchMain.BenchmarkSmokeResult result =
        MlpBenchMain.runBenchmarkSmoke(tempDir.resolve("load-failure-reports"), manifest);

    assertEquals(RunOutcome.ADAPTER_ERROR, result.records().get(2).outcome());
    assertTrue(Files.readString(result.csvPath()).contains("could not load public benchmark file"));
  }

  @Test
  void benchmarkSmokeReportsRelativeMissingPublicInput() throws IOException {
    Path manifest = tempDir.resolve("relative-manifest.json");
    Files.writeString(
        manifest,
        """
                {
                  "instances": [
                    {
                      "id": "relative-missing",
                      "format": "MPS",
                      "localPath": "missing-local.mps",
                      "status": "approved-local"
                    }
                  ]
                }
                """);

    MlpBenchMain.BenchmarkSmokeResult result =
        MlpBenchMain.runBenchmarkSmoke(tempDir.resolve("relative-reports"), manifest);

    assertEquals(RunOutcome.SOLVER_UNAVAILABLE, result.records().get(2).outcome());
    assertTrue(result.records().get(2).failureMessage().contains("missing-local.mps"));
  }

  @Test
  void benchmarkSmokeRejectsMalformedManifest() throws IOException {
    Path emptyManifest = tempDir.resolve("empty-manifest.json");
    Path missingFieldManifest = tempDir.resolve("missing-field-manifest.json");
    Files.writeString(emptyManifest, "{\"instances\": []}");
    Files.writeString(
        missingFieldManifest,
        """
                {
                  "instances": [
                    {
                      "id": "bad",
                      "format": "MPS",
                      "status": "approved-local"
                    }
                  ]
                }
                """);

    assertThrows(
        IOException.class,
        () -> MlpBenchMain.runBenchmarkSmoke(tempDir.resolve("empty-reports"), emptyManifest));
    assertThrows(
        IOException.class,
        () ->
            MlpBenchMain.runBenchmarkSmoke(
                tempDir.resolve("missing-field-reports"), missingFieldManifest));
  }
}
