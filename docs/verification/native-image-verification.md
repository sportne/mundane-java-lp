# Native Image Verification

The native lane is explicit because GraalVM `native-image` is optional on local
machines and CI workers.

Run:

```bash
./gradlew nativeSmoke --console=plain
```

If `native-image` is absent, the lane reports a clean skip. If it is present,
the lane builds and runs the native CLI smoke executable. The smoke validates a
deterministic generated LP through the same validation engine used by JVM
checks.

Still deferred:

- tiny MPS validation from the native executable;
- shared-library load checks from C;
- shared-library create/free cycles;
- normalized native solver result parity with JVM solver runs.
