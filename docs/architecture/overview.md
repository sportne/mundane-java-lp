# Architecture overview

The project is organized around a one-way dependency flow:

```text
lp-model + lp-sparse
        ↓
lp-validation + lp-io
        ↓
lp-solver-spi
        ↓
lp-harness-api
        ↓
lp-harness-cli + adapters + reports
```

The eventual Java-native solver must enter through the same solver SPI as every
other solver. It is not allowed to bypass validation or benchmark reporting.
