package io.github.mundanej.mlp.io.mps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MpsReaderTest {
    @TempDir
    Path tempDir;

    @Test
    void readsMinimalNameAndEndata() throws Exception {
        Path path = tempDir.resolve("tiny.mps");
        Files.writeString(path, "NAME TINY\nENDATA\n");
        assertEquals("TINY", new MpsReader().read(path).name());
    }
}
