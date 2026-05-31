package io.github.mundanej.mlp.examples.nativecli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class NativeCliSmokeMainTest {
    @Test
    void nativeCliSmokeValidatesGeneratedInstance() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            NativeCliSmokeMain.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("native cli smoke"));
        assertTrue(text.contains("instance=network-flow-3-node-seed-7"));
        assertTrue(text.contains("validation=accepted"));
    }
}
