package io.github.mundanej.mlp.harness.report;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

final class MarkdownReportWriterTest {
    @Test
    void rendersEmptyReport() {
        assertTrue(new MarkdownReportWriter().render(List.of()).contains("LP benchmark report"));
    }
}
