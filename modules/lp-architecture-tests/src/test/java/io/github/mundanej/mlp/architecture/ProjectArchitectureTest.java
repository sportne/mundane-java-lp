package io.github.mundanej.mlp.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

final class ProjectArchitectureTest {
    private static final Path ROOT = Path.of(System.getProperty("user.dir")).getParent().getParent();

    @Test
    void onlyCliAdaptersUseProcessBuilder() throws IOException {
        List<Path> offenders = javaFiles()
                .filter(path -> contains(path, "ProcessBuilder"))
                .filter(path -> !path.toString().contains("lp-adapter-highs-cli"))
                .filter(path -> !path.toString().contains("lp-adapter-clp-cli"))
                .filter(path -> !path.toString().contains("lp-adapter-glpk-cli"))
                .filter(path -> !path.toString().contains("lp-architecture-tests"))
                .toList();
        assertTrue(offenders.isEmpty(), () -> "ProcessBuilder outside CLI adapters: " + offenders);
    }

    @Test
    void nativeTargetedCodeAvoidsReflectionTokens() throws IOException {
        List<String> forbidden = List.of("Class.forName", "java.lang.reflect", "Proxy.newProxyInstance", "Unsafe");
        List<Path> offenders = javaFiles()
                .filter(path -> isNativeTargeted(path))
                .filter(path -> forbidden.stream().anyMatch(token -> contains(path, token)))
                .toList();
        assertTrue(offenders.isEmpty(), () -> "Native-targeted forbidden token use: " + offenders);
    }

    private static boolean isNativeTargeted(final Path path) {
        String value = path.toString();
        return value.contains("lp-model")
                || value.contains("lp-sparse")
                || value.contains("lp-validation")
                || value.contains("lp-native-api");
    }

    private static Stream<Path> javaFiles() throws IOException {
        return Files.walk(ROOT.resolve("modules"))
                .filter(path -> path.toString().endsWith(".java"));
    }

    private static boolean contains(final Path path, final String token) {
        try {
            return Files.readString(path).contains(token);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
