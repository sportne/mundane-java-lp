# Native Image Verification

The native lane is explicit because GraalVM `native-image` is optional on local
machines and CI workers.

Run:

```bash
./gradlew nativeSmoke --console=plain
```

If `native-image` is absent, the lane reports a clean skip. If it is present,
the lane delegates the executable build and run to the GraalVM Native Build
Tools Gradle plugin. The smoke validates a deterministic generated LP through
the same validation engine used by JVM checks.

The native build convention disables the plugin reachability metadata
repository. A native smoke failure should therefore be addressed in code shape
or documented explicitly before adding metadata workarounds.

Still deferred:

- tiny MPS validation from the native executable;
- shared-library load checks from C;
- shared-library create/free cycles;
- normalized native solver result parity with JVM solver runs.
