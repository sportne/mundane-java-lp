# Native Image verification

G0 native verification is a scaffold lane that checks whether `native-image` is
present and records the native checks that G8 must make meaningful.

G8 native verification should cover:

- CLI native executable starts.
- CLI native executable validates a tiny MPS.
- CLI native executable runs a generated tiny instance.
- Shared library loads from C smoke test.
- Shared library handles create/free cycles safely.
- Native runs produce the same normalized result shape as JVM runs.
