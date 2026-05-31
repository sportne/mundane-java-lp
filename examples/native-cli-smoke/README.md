# native-cli-smoke

Example project for native executable smoke checks.

The JVM entrypoint validates the deterministic generated network-flow fixture
and prints a compact status summary. The `nativeSmoke` Gradle task builds and
runs this entrypoint with GraalVM `native-image` when the tool is available, and
skips cleanly when it is not.
