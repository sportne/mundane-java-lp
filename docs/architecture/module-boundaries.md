# Module boundaries

## Foundation modules

- `lp-model`: canonical LP domain model; no solver or I/O behavior.
- `lp-sparse`: primitive sparse matrix containers and kernels.
- `lp-validation`: solution and status validation.
- `lp-io-mps`: MPS reading and writing.
- `lp-io-lp`: LP-format reading and writing placeholder.

## Harness modules

- `lp-solver-spi`: adapter contract.
- `lp-solver-simple`: in-project correctness-first simple solver adapter.
- `lp-solver-performance`: in-project performance-oriented solver adapter
  following ADR-0007.
- `lp-harness-api`: benchmark orchestration model.
- `lp-harness-cli`: command-line entrypoint.
- `lp-harness-report`: JSON/CSV/Markdown report output.

## Adapter modules

- CLI adapter modules may use `ProcessBuilder`.
- Java adapter modules may add external solver/library dependencies inside the
  adapter module only.
- In-project solver modules must not execute external processes or depend on
  third-party solver libraries.
- The performance solver must keep solver state in primitive arrays and expose
  results only through the solver SPI.
- Adapters must normalize status, objective, timing, and logs.

## Native modules

- `lp-native-api` is the planned GraalVM C ABI boundary.
- `lp-native-tests` contains native smoke tests and fixtures.

## Forbidden dependency directions

- Foundation modules must not depend on harness or adapters.
- `lp-sparse` must not depend on I/O, validation, harness, or adapters.
- `lp-model` must not depend on sparse storage implementation details.
- Adapters must not mutate global JVM state.
- Native-targeted modules must not use reflection, classpath scanning, dynamic
  class loading, dynamic proxies, Java serialization, JNI, `Unsafe`, or
  internal JDK APIs without an ADR.
