# How AI was used

> **Draft: rewrite this in your own words before submitting.** Reviewers will ask about it, and it should describe what you actually did.

## Tools

- **Claude (Anthropic)** as a pair programmer: proposing designs, generating first drafts of code and tests, and explaining trade-offs.
- **Angular CLI and Spring Initializr** for project scaffolding.

## How I worked with it

1. **Requirements first.** I wrote the requirements document before any code and used it as context for later prompts, so generated code stayed within the agreed scope.
2. **Small, reviewable steps.** Each step (schema, entities, seed, CRUD API, insights API, UI) was generated, run locally, tested and committed on its own. The commit history mirrors this.
3. **Running everything myself.** Every change was compiled, tested and exercised locally before committing. AI suggestions were treated as drafts, not answers.
4. **Asking "why".** For each design choice I asked for the reasoning and alternatives, and recorded the ones I agreed with in DECISIONS.md.

## Where AI output needed correcting

These came up during development and were fixed by reading the errors and the actual project state:

- **Port clash with a local Postgres.** The first Docker setup used port 5432, which was already taken by a Postgres installed on my machine, so the app connected to the wrong database ("password authentication failed"). Moved Docker Postgres to 5433.
- **Seeder assumed database defaults that didn't exist.** The generated seeder relied on `DEFAULT now()` for `created_at`, but my actual V1 migration had no default. Fixed by setting timestamps explicitly in the seeder. I did not edit the applied migration, since Flyway checksums would break.
- **Schema drift.** My V1 migration differed from the AI's draft (no query indexes, salary allowed to be zero). Added a V3 migration rather than rewriting history.
- **Framework version details.** Spring Boot 4 generates a package-private Testcontainers config class, which tests in other packages couldn't use, so it was made public. Angular 22 required a newer Node.js than I had installed.

## Example prompts

- "Here is my requirements doc. Propose a database schema and explain each decision."
- "Write a seed generator for 10,000 employees that is deterministic and realistic per country and role, and keep it testable without a database."
- "Tests are failing with this log: [log]. What is the root cause?"
- "What would a reviewer criticise in this API design?"
