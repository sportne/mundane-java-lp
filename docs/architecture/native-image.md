# GraalVM Native Image architecture

Native Image support is a project constraint, not a late packaging step.

## 0.1.0 posture

The native lane is meaningful for command-line startup and validation smoke.
Native executable builds are owned by the official GraalVM Native Build Tools
Gradle plugin. The project-level `nativeSmoke` task keeps a small wrapper around
that plugin so non-GraalVM environments skip cleanly, while opted-in executable
projects build and run through the plugin's `nativeRun` path.

The plugin's reachability metadata repository is disabled for 0.1.0. Native
support must come from code shape and documented architecture decisions rather
than downloaded metadata workarounds.

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
- Native command-line executable smoke through the GraalVM Gradle plugin.
- Native shared library with a small C ABI.

The C ABI should expose opaque handles and primitive arrays, not Java object
references.
