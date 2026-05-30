# Native Image verification

G0 native verification is a placeholder lane that checks whether `native-image`
is present and records that real native builds are deferred.

Future native verification should cover:

- CLI native executable starts.
- CLI native executable validates a tiny MPS.
- CLI native executable runs a generated tiny instance.
- Shared library loads from C smoke test.
- Shared library handles create/free cycles safely.
- Native runs produce the same normalized result shape as JVM runs.
