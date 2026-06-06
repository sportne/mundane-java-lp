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
        Path graalHome = projectDir.resolve("fake-graalvm");
        writeFakeGraalVm(graalHome);
        writeApplicationProject(false);

        BuildResult result = runner().withEnvironment(environmentWithGraalHome(graalHome)).build();

        assertTrue(result.getOutput().contains("native-image available; no application smoke target."));
    }

    @Test
    void nativeSmokeBuildsAndRunsExecutableWhenOptedIn() throws IOException {
        Path graalHome = projectDir.resolve("fake-graalvm");
        writeFakeGraalVm(graalHome);
        writeApplicationProject(true);

        BuildResult result = runner().withEnvironment(environmentWithGraalHome(graalHome)).build();

        assertTrue(result.getOutput().contains("fake native executable ran"));
        assertTrue(Files.exists(projectDir.resolve("build/native/nativeCompile/native-test")));
    }

    @Test
    void nativeProfileMetadataReportsOptimizedArgs() throws IOException {
        writeApplicationProject(true);

        BuildResult result = runner(
                        "nativeProfileMetadata",
                        "-Pmlp.native.profile=optimized",
                        "--console=plain",
                        "--configuration-cache")
                .withEnvironment(environmentWithPath(projectDir.resolve("empty-bin")))
                .build();

        assertTrue(result.getOutput().contains("nativeProfile=optimized"));
        assertTrue(result.getOutput().contains("nativeProfileArgs=-O3"));
        assertTrue(result.getOutput().contains("native-image unavailable; profile build would skip"));
    }

    @Test
    void nativeProfileMetadataReportsPgoInstrumentArgs() throws IOException {
        writeApplicationProject(true);
        String profile = projectDir.resolve("build/native/pgo/train.iprof").toString();

        BuildResult result = runner(
                        "nativeProfileMetadata",
                        "-Pmlp.native.profile=pgo-instrument",
                        "-Pmlp.native.pgoProfile=" + profile,
                        "--console=plain",
                        "--configuration-cache")
                .withEnvironment(environmentWithPath(projectDir.resolve("empty-bin")))
                .build();

        assertTrue(result.getOutput().contains("nativeProfile=pgo-instrument"));
        assertTrue(result.getOutput().contains("--pgo-instrument"));
        assertTrue(result.getOutput().contains("nativeRuntimeProfileArgs=-XX:ProfilesDumpFile=" + profile));
    }

    @Test
    void nativeProfileMetadataReportsPgoOptimizedArgs() throws IOException {
        writeApplicationProject(true);
        String profile = projectDir.resolve("build/native/pgo/train.iprof").toString();

        BuildResult result = runner(
                        "nativeProfileMetadata",
                        "-Pmlp.native.profile=pgo-optimized",
                        "-Pmlp.native.pgoProfile=" + profile,
                        "--console=plain",
                        "--configuration-cache")
                .withEnvironment(environmentWithPath(projectDir.resolve("empty-bin")))
                .build();

        assertTrue(result.getOutput().contains("nativeProfile=pgo-optimized"));
        assertTrue(result.getOutput().contains("--pgo=" + profile));
    }

    private GradleRunner runner() {
        return runner("nativeSmoke", "--console=plain", "--configuration-cache");
    }

    private GradleRunner runner(final String... arguments) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments(arguments);
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

    private static Map<String, String> environmentWithGraalHome(final Path graalHome) {
        Map<String, String> environment = new HashMap<>(System.getenv());
        environment.put("GRAALVM_HOME", graalHome.toString());
        environment.put("JAVA_HOME", graalHome.toString());
        return environment;
    }

    private static void writeFakeGraalVm(final Path graalHome) throws IOException {
        writeExecutableSymlink(graalHome.resolve("bin/java"), Path.of(System.getProperty("java.home"), "bin", "java"));
        writeExecutableSymlink(graalHome.resolve("bin/javac"), Path.of(System.getProperty("java.home"), "bin", "javac"));
        writeFakeNativeImage(graalHome.resolve("bin"));
    }

    private static void writeExecutableSymlink(final Path link, final Path target) throws IOException {
        Files.createDirectories(link.getParent());
        Files.deleteIfExists(link);
        if (Files.exists(target)) {
            Files.createSymbolicLink(link, target);
        }
    }

    private static void writeFakeNativeImage(final Path bin) throws IOException {
        Files.createDirectories(bin);
        Path nativeImage = bin.resolve("native-image");
        Files.writeString(nativeImage, """
                #!/bin/sh
                for argument in "$@"; do
                  if [ "$argument" = "--version" ]; then
                    echo "GraalVM 21.0.2 Java 21.0.2"
                    exit 0
                  fi
                done
                output=""
                image_path=""
                image_name=""
                previous=""
                last=""
                for argument in "$@"; do
                  if [ "$previous" = "-o" ]; then
                    output="$argument"
                  fi
                  case "$argument" in
                    -H:Path=*) image_path="${argument#-H:Path=}" ;;
                    -H:Name=*) image_name="${argument#-H:Name=}" ;;
                  esac
                  previous="$argument"
                  last="$argument"
                done
                if [ -z "$output" ] && [ -n "$image_path" ] && [ -n "$image_name" ]; then
                  output="$image_path/$image_name"
                fi
                if [ -z "$output" ]; then
                  output="$last"
                fi
                mkdir -p "$(dirname "$output")"
                cat > "$output" <<'SCRIPT'
                #!/usr/bin/env sh
                echo "fake native executable ran"
                SCRIPT
                chmod +x "$output"
                """);
        nativeImage.toFile().setExecutable(true);
    }
}
