package io.github.mundanej.mlp.harness.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.harness.RunOutcome;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MlpBenchMainTest {
    @TempDir
    private Path tempDir;

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

        assertEquals(4, result.records().size());
        assertEquals(RunOutcome.SUCCESS, result.records().getFirst().outcome());
        assertEquals(RunOutcome.SOLVER_UNAVAILABLE, result.records().get(1).outcome());
        assertTrue(Files.readString(result.markdownPath()).contains("benchmark-smoke-generated"));
        assertTrue(Files.readString(result.jsonPath()).contains("\"instance\":\"netlib-afiro\""));
        assertTrue(Files.readString(result.csvPath()).contains("missing local public benchmark file"));
    }

    @Test
    void benchmarkSmokeLoadsPresentPublicMpsFile() throws IOException {
        Path repoRoot = tempDir.resolve("repo");
        Path manifest = repoRoot.resolve("instances/public/manifest.example.json");
        Path publicFile = repoRoot.resolve("instances/public/netlib/afiro.mps");
        Files.createDirectories(publicFile.getParent());
        Files.writeString(publicFile, """
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
        Files.writeString(manifest, """
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

        MlpBenchMain.BenchmarkSmokeResult result = MlpBenchMain.runBenchmarkSmoke(tempDir.resolve("reports"), manifest);

        assertEquals(2, result.records().size());
        assertEquals(RunOutcome.SUCCESS, result.records().getFirst().outcome());
        assertEquals(RunOutcome.SOLVER_UNAVAILABLE, result.records().get(1).outcome());
        assertTrue(Files.readString(result.csvPath()).contains("public benchmark loaded"));
    }
}
