# Architecture rule catalog

This catalog records architecture rules that should be mechanically enforced
with architecture tests or source-shape tests where practical. Rules apply to
production main code unless an entry says otherwise. Any new exception must be
documented here or in an ADR in the same change.

## Project-specific rules

| Rule | Rationale | Enforced scope | Allowed exceptions | Test evidence |
|---|---|---|---|---|
| `lp-model` must not depend on harness, adapter, native, or example modules. | The canonical model is foundational. | `modules/lp-model` main code. | None. | `ProjectArchitectureTest`. |
| `lp-sparse` must use primitive arrays for storage types. | Massive LP kernels cannot rely on object-heavy sparse entries. | `modules/lp-sparse` main code. | Builders may use temporary collections before materialization. | `ProjectArchitectureTest`. |
| Only CLI adapter modules may use `ProcessBuilder`. | Process execution is an adapter boundary. | All main source. | `lp-adapter-*-cli` modules and CLI examples. | `ProjectArchitectureTest`. |
| In-project solver modules must not execute external processes. | In-project solvers are library code, not binary wrappers. | Solver modules under `modules/lp-solver-*`, excluding `lp-solver-spi`. | None. | `ProjectArchitectureTest`. |
| Only CLI entrypoints may call `System.exit`. | Libraries must return diagnostics instead of terminating hosts. | All main source. | Documented CLI main classes. | `ProjectArchitectureTest`. |
| Adapters must return normalized `SolverRunResult`. | Benchmark and validation behavior must be comparable. | Adapter modules. | None. | Future adapter tests. |
| Benchmark reports must record solver ID, version, options, tolerance profile, timing, and machine metadata. | Benchmark claims need context. | Harness/report modules. | None. | Future report tests. |

## GraalVM Native Image rules

| Rule | Rationale | Enforced scope | Allowed exceptions | Test evidence |
|---|---|---|---|---|
| Native-targeted code must not use reflection, dynamic proxies, `Class.forName`, `ClassLoader`, `URLClassLoader`, `ServiceLoader`, or `MethodHandles.lookup`. | These mechanisms commonly require reachability metadata and hide runtime discovery. | `lp-model`, `lp-sparse`, `lp-validation`, `lp-native-api` main code. | None without ADR. | `ProjectArchitectureTest`. |
| Native-targeted code must not use Java serialization. | Serialization needs metadata and is unnecessary for the core. | `lp-model`, `lp-sparse`, `lp-validation`, `lp-native-api` main code. | None. | `ProjectArchitectureTest`. |
| Native-targeted code must not use JNI/native methods, internal JDK APIs, `System.load`, `System.loadLibrary`, or `Unsafe`. | These APIs are brittle across JDKs and native-image configurations. | `lp-model`, `lp-sparse`, `lp-validation`, `lp-native-api` main code. | None without ADR. | `ProjectArchitectureTest`. |
| Native-targeted modules must not ship reachability-metadata workaround files in 0.1.0. | The core should avoid metadata needs by design. | `modules/*/src/main/resources/META-INF/native-image`. | None without ADR. | `ProjectArchitectureTest`. |

## General Java baseline rules

| Rule | Rationale | Enforced scope | Allowed exceptions | Test evidence |
|---|---|---|---|---|
| No finalizers. | Finalization is deprecated and nondeterministic. | All production modules. | None. | `ProjectArchitectureTest`. |
| No public static mutable fields. | Global mutable state makes behavior order-dependent. | All production modules. | Constants only. | Future source-shape tests. |
| No global JVM mutation. | Libraries must not mutate process-wide properties, streams, locale, or timezone. | All production modules. | CLI entrypoints may configure logging later with ADR. | Future tests. |
| Public API declarations need Javadoc. | Public classes, records, interfaces, enums, methods, fields, constructors, parameters, and enum constants must be understandable without reading implementation code. | Main Java source in modules and examples. | `main` methods may rely on the enclosing class summary; overrides may use `{@inheritDoc}`. | Source-shape test for declaration, enum-constant, method parameter, constructor parameter, and record component Javadocs. |
