# Spotless configuration

Spotless is enabled for Java library and application projects through the
shared Gradle build logic.

Java sources use Google Java Format in its default Google code style. The build
also removes unused imports, applies the project import order, trims trailing
whitespace, converts leading tabs to spaces, and requires final newlines.

Gradle and Markdown formatting is intentionally limited to whitespace hygiene:
trailing whitespace, leading tabs, and final newlines. It does not reflow prose.

`spotlessCheck` is part of the standard quality gate. Use `spotlessApply` only
for intentional formatting changes and review the resulting diff before commit.
