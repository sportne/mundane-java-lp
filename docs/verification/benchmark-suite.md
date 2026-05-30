# Benchmark suite

Benchmark runs must report:

- instance ID and source;
- generator parameters where applicable;
- solver ID and version;
- solver options;
- tolerance profile;
- parse time;
- canonicalization/export time;
- solve time;
- validation time;
- total wall time;
- peak memory where measured;
- objective and residuals;
- status and termination reason;
- machine metadata.

Do not combine parsing/export time with solve time without also reporting them
separately.
