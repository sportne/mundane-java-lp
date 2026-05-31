# SpotBugs configuration

SpotBugs is enabled for Java library and application projects through the shared
Gradle build logic.

`spotbugsMain` is strict and participates in the standard quality gate.
`spotbugsTest` produces XML and HTML reports but is report-only during the
initial rollout so test-helper findings can be triaged without blocking
unrelated work.

Suppressions belong in `exclude.xml` only for intentional false positives. Keep
each suppression narrow enough to identify the owning class or method and the
specific bug pattern.
