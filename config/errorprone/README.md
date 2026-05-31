# Error Prone configuration

Error Prone is not enabled in the 0.1.0 build. The current quality gate relies
on compiler warnings as errors, Checkstyle, Spotless, SpotBugs, architecture
tests, and coverage verification.

When Error Prone is introduced, configure it through the shared Gradle build
logic so all Java library and application projects use the same rule set.
