# Contributing

This project uses design-control gates. Contributions should be small,
traceable, and tied to documented requirements.

## Before coding

1. Find or create a roadmap task under `docs/roadmap/tasks/`.
2. Confirm the target module and allowed files.
3. Confirm required tests and docs updates.
4. Run the acceptance command recorded by the task.

## Pull request expectations

A pull request should include:

- requirement IDs or roadmap task ID;
- summary of behavior changed;
- tests added or updated;
- documentation updated;
- benchmark evidence if performance is discussed;
- exact command used to validate the change.

## Benchmark claims

Benchmark claims must record:

- machine metadata;
- solver versions;
- model source or generator parameters;
- tolerance profile;
- wall time and solve time definitions;
- memory measurement method;
- failures and skipped solvers.

Do not remove failing or unfavorable solver results from reports.
