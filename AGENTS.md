# Basic FHIR Server — agent instructions

Spring Boot 4 / Java 17 FHIR R4 server (HAPI FHIR 8.0.0) over a legacy OpenEMR
MariaDB schema, exposed as FHIR resources.

## Build & test

Always use the Maven wrapper — never bare `mvn`.

```bash
./mvnw compile                              # compile only
./mvnw test                                 # full suite
./mvnw test -Dtest='*ResourceProviderTest'  # provider unit tests only
./mvnw test -Dtest='*ServiceImplTest'       # service unit tests only
./mvnw spotless:apply                       # format (google-java-format, 2-space indent)
```

`spotless:check` runs in the `verify` phase, so `./mvnw verify` fails on unformatted
code. Format before you commit.

## Code style

- 2-space indent, Google Java Format (enforced by Spotless).
- Mirror the existing implementation for the resource you touch. Do not invent
  a different shape, and do not "improve" on a reference you were told to copy.
- Keep changes scoped to the task. Do not refactor adjacent code, add validation
  for impossible states, or add abstractions for a single call site.

## Architecture

Per resource, the chain is:

```
provider -> service -> repository -> mapper
```

The search path adds a criteria/translator/query trio:

```
provider -> <Resource>SearchCriteria -> <Resource>SearchTranslator
         -> <Resource>SearchQuery -> repository
```

- Reference implementations: `PatientResourceProvider` (canonical),
  `AllergyIntoleranceProvider` (`_include` and search).
- Pagination defaults live in the **repository**, never in the provider. The
  repository always pages — no unpaged branch.
- Profile URIs live in `mapper/utils/ProfilesConstants`; system/code URIs live in
  `mapper/utils/FhirCodeSystemConstants`. Never inline a URI string.
- Providers advertise profiles with the project-local `@SupportedProfiles`
  annotation (same package as the providers), not HAPI's.
- Unit-test a provider by mocking every collaborator (service, mapper,
  translator) and exercising only the provider's own branching. Integration
  tests drive the real Spring bean through its public methods and assert only on
  the returned FHIR resource.
- Do not read, grep, or modify the OpenEMR SQL dump. Learn the table/column
  shape from the repository classes.

## Parallel agents — worktree rules

You are running inside a **git worktree**, not the main checkout. Your branch is
`agent/<name>`. The worktree directory is your sandbox: other agents have their
own directory, their own index and their own branch, so your edits cannot
collide with theirs.

Worktrees live in `<repo>/.worktrees/<name>` and are created with
`./scripts/worktree.sh add <name>` from the main checkout. That script is the
human's tool — do not run it, and do not run it from inside a worktree.

**Do**

- Commit to your own branch, early and often. Your branch is yours alone.
- Run only the tests for the area you are working on.
- Keep changes inside your assigned resource/area. If you must touch a shared
  file (for example `ProfilesConstants`), keep the change additive and call it
  out in your commit message.
- Report what you changed and what you left alone.

**Do not**

- `git stash` — the stash stack is **repo-global** and shared by every worktree.
  You would be popping another agent's work. Commit instead.
- `git checkout` / `git switch` to another branch, or touch `main`. Merging into
  `main` is the human's job, not yours.
- `git branch -D`, `git worktree add`, `git worktree remove`, `git worktree prune`
  — creating and destroying worktrees is the human's job.
- Modify `scripts/worktree.sh`, `AGENTS.md`, or `src/test/resources/application.yaml`.
- Read or modify the OpenEMR SQL dump.

## Shared services

`src/test/resources/application.yaml` disables JobRunr and points the datasource
at a throwaway database, so `mvn test` is safe to run concurrently in several
worktrees. The Testcontainers integration test spins up its own MariaDB
container per worktree and is likewise isolated.

If you need to boot the actual app (not just the tests), you must isolate
yourself explicitly — the defaults in `src/main/resources/application.yaml` point
at the shared OpenEMR database and fixed ports:

```bash
SERVER_PORT=8081 \
SPRING_DATASOURCE_URL='jdbc:mariadb://localhost:8320/openemr_<yourname>' \
JOBRUNR_DASHBOARD_PORT=8001 \
./mvnw spring-boot:run
```

Leave `jobrunr.background-job-server` disabled in a worktree unless you are
specifically working on `jobs/export/`.
