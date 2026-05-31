# GraalVM Native Image architecture

Native Image support is a project constraint, not a late packaging step.

## 0.1.0 posture

The native lane is meaningful for command-line startup and validation smoke. The
`nativeSmoke` Gradle task checks for GraalVM `native-image`; when the tool is
absent it skips cleanly, and when present it builds and runs the
`examples/native-cli-smoke` executable against a deterministic generated LP.

Native shared-library and C ABI checks remain future work.

## Native-targeted constraints

Native-targeted code should avoid:

- reflection;
- classpath scanning;
- dynamic class loading;
- dynamic proxies;
- Java serialization;
- JNI or native methods;
- internal JDK APIs;
- `Unsafe`;
- hidden resource discovery.

Architecture tests enforce these constraints for native-targeted main code:
`lp-model`, `lp-sparse`, `lp-validation`, and `lp-native-api`. Those modules
also must not ship `META-INF/native-image` reachability metadata workaround
files without a documented architecture decision.

## Planned targets

- JVM jars.
- Native command-line executable smoke.
- Native shared library with a small C ABI.

The C ABI should expose opaque handles and primitive arrays, not Java object
references.
