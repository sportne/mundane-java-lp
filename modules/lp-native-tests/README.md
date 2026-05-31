# lp-native-tests

Native smoke test module for GraalVM executable and library checks.

The module keeps JVM-level checks for the native API surface. Executable
native-image smoke coverage is owned by `examples/native-cli-smoke:nativeSmoke`,
which delegates executable builds to the GraalVM Native Build Tools plugin.
