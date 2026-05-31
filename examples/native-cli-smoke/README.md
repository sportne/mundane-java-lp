# native-cli-smoke

Example project for native executable smoke checks.

The JVM entrypoint validates the deterministic generated network-flow fixture
and prints a compact status summary. The `nativeSmoke` Gradle task delegates the
native executable build and run to the GraalVM Native Build Tools plugin when
`native-image` is available, and skips cleanly when it is not.
