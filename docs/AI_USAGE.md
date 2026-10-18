# How AI was used

The assessment asked for AI tools to be used intentionally, so this is an honest account of how I used them, including where the AI was wrong.

## Tools

- **Claude (claude.ai)** as a pair programmer throughout: planning, drafting documents, generating code and tests, explaining errors and trade-offs. I worked with it conversationally, mostly in Hinglish.
- **Spring Initializr** and **Angular CLI** for scaffolding the backend and frontend projects.

## How I worked

**One small step at a time.** I didn't ask for the whole application at once. The order was: requirements → Spring Boot scaffold → local Postgres → schema → entities and tests → seed data → CRUD API → insights API → UI → docs → Docker and deployment. After each step I ran it on my machine, confirmed tests passed, and committed. The commit history follows this order.

**Requirements before code.** The first commit is the requirements document. Claude drafted it from the assessment brief, and it fixed the scope early: what to build, and what to leave out on purpose (authentication, salary history, Excel import). Later steps were checked against it.

**I ran everything and reported back what actually happened.** Code generation was AI-assisted, but every command was run by me, on my machine. When something failed I pasted the real error output, and we fixed the root cause from there rather than guessing.

**Asking why.** I asked the AI to explain decisions, not just make them: why a requirements document first, why I shouldn't put my own database password in the config, why median instead of average, why salary sorting is disabled without a country filter. The ones I agreed with are recorded in [DECISIONS.md](DECISIONS.md), and I prepared to explain each of them.

## Where the AI's assumptions didn't match reality

Most problems came from the AI assuming something about my environment or my files that wasn't true. Reading the actual error was always what found the cause.

| What happened | Cause | Fix |
|---|---|---|
| App failed with "password authentication failed for user acme" | A PostgreSQL already installed on my machine was using port 5432, so the app connected to it instead of the Docker database | Moved the Docker database to port 5433 |
| Seeder tests failed: `created_at` cannot be null | The seeder assumed the database sets `created_at` by default, but my V1 migration had no default | Seeder now sets timestamps itself. V1 was not edited, because Flyway rejects changes to applied migrations |
| My V1 migration differed from the AI's version | No query indexes, and salary was allowed to be zero | Asked to see my real file, then added a V3 migration instead of rewriting V1 |
| Tests in other packages couldn't use the Testcontainers config | Spring Boot 4 generates that class as package-private | Made it public |
| Angular CLI refused to run | Angular 22 needs a newer Node.js than I had (22.17) | Upgraded to Node 24 rather than downgrading Angular |
| Pay currency dropdown was empty | The new exchange-rates endpoint existed in code, but the running backend was started before it was added | Restarted the backend |
| Docker container couldn't reach the database through `host.docker.internal` | Same local Postgres conflict | Ran the container on the Compose network and used the service name |
| Render deploy failed: database "salary" does not exist | Render had given the database a different name | Used the name shown in Render's connection details |
| `git push` was rejected | I had edited the README on GitHub's website, so the remote had commits my machine didn't | `git pull --rebase`, then push |

## What the AI did well

- Turned the brief into a clear plan with a sensible order of work.
- Suggested product details I wouldn't have thought of at first: median over average, comparing outliers in local currency, and blocking misleading salary sorts across currencies.
- Wrote tests at the right levels, with small datasets where the expected numbers can be checked by hand.
- Explained errors step by step, which helped me learn Docker, Flyway and Git workflows along the way.

## What I'd do differently

- **Check tool versions first** (Java, Node, Docker) before scaffolding, to avoid version errors midway.
- **Show the AI my real files earlier.** The V1 mismatch would have been caught sooner if I had shared the file before building on top of it.
- **Edit in one place.** Editing on GitHub and locally at the same time caused the rejected push.

## Example prompts

These are close to what I actually asked, translated from Hinglish:

- "I got this assessment from a company. Help me approach it." (followed by choosing Java and Spring Boot)
- "Why did you create the requirements file first?"
- "How do I commit this?"
- "[pasted test log] What is the real error here?"
- "Write the README again for me with these changes."