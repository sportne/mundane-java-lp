package io.github.mundanej.mlp.buildlogic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class NativeConventionsPluginTest {
    @TempDir
    private Path projectDir;

    @Test
    void nativeSmokeSkipsWhenNativeImageIsUnavailable() throws IOException {
        writeApplicationProject(false);

        BuildResult result = runner()
                .withEnvironment(environmentWithPath(projectDir.resolve("empty-bin")))
                .build();

        assertTrue(result.getOutput().contains("native-image unavailable; skipping native executable smoke."));
    }

    @Test
    void nativeSmokeSkipsApplicationWithoutExecutableOptIn() throws IOException {
        Path bin = projectDir.resolve("bin");
        writeFakeNativeImage(bin);
        writeApplicationProject(false);

        BuildResult result = runner()
                .withEnvironment(environmentWithPath(bin, true))
                .build();

        assertTrue(result.getOutput().contains("native-image available; no application smoke target."));
    }

    @Test
    void nativeSmokeBuildsAndRunsExecutableWhenOptedIn() throws IOException {
        Path bin = projectDir.resolve("bin");
        writeFakeNativeImage(bin);
        writeApplicationProject(true);

        BuildResult result = runner()
                .withEnvironment(environmentWithPath(bin, true))
                .build();

        assertTrue(result.getOutput().contains("fake native executable ran"));
        assertTrue(Files.exists(projectDir.resolve("build/native/nativeSmoke/native-test")));
    }

    private GradleRunner runner() {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("nativeSmoke", "--console=plain", "--configuration-cache");
    }

    private void writeApplicationProject(final boolean executableSmoke) throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'native-test'\n");
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'application'
                    id 'mlp.native-conventions'
                }

                application {
                    mainClass = 'example.Main'
                }
                """);
        if (executableSmoke) {
            Files.writeString(projectDir.resolve("build.gradle"), """
                    plugins {
                        id 'application'
                        id 'mlp.native-conventions'
                    }

                    application {
                        mainClass = 'example.Main'
                    }

                    mlpNativeSmoke {
                        executableSmoke = true
                    }
                    """);
        }
        Path source = projectDir.resolve("src/main/java/example/Main.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package example;

                public final class Main {
                    private Main() {
                    }

                    public static void main(final String[] args) {
                        System.out.println("jvm main");
                    }
                }
                """);
    }

    private static Map<String, String> environmentWithPath(final Path path) throws IOException {
        return environmentWithPath(path, false);
    }

    private static Map<String, String> environmentWithPath(final Path path, final boolean includeSystemPath)
            throws IOException {
        Files.createDirectories(path);
        Map<String, String> environment = new HashMap<>(System.getenv());
        String systemPath = System.getenv("PATH");
        if (includeSystemPath && systemPath != null && !systemPath.isBlank()) {
            environment.put("PATH", path + java.io.File.pathSeparator + systemPath);
        } else {
            environment.put("PATH", path.toString());
        }
        environment.remove("GRAALVM_HOME");
        return environment;
    }

    private static void writeFakeNativeImage(final Path bin) throws IOException {
        Files.createDirectories(bin);
        Path nativeImage = bin.resolve("native-image");
        Files.writeString(nativeImage, """
                #!/bin/sh
                output="$5"
                cat > "$output" <<'SCRIPT'
                #!/usr/bin/env sh
                echo "fake native executable ran"
                SCRIPT
                chmod +x "$output"
                """);
        nativeImage.toFile().setExecutable(true);
    }
}
