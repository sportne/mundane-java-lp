# GraalVM Native Image architecture

Native Image support is a project constraint, not a late packaging step.

## G0 posture

G0 includes only native smoke placeholders. Real native executable and shared
library builds are introduced after the harness CLI and model validation are
stable.

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

## Planned targets

- JVM jars.
- Native command-line executable.
- Native shared library with a small C ABI.

The C ABI should expose opaque handles and primitive arrays, not Java object
references.
