# Project Notes

## Build

This is a Gradle (Groovy DSL) + Spring Boot 3 + Java 21 project.

```
./gradlew build
```

### Windows / corporate network SSL note

If Gradle fails to download dependencies or the Gradle distribution with:

```
PKIX path building failed: ... unable to find valid certification path to requested target
```

it means the JDK's default `cacerts` truststore doesn't trust your network's TLS
interception certificate (even though Windows/PowerShell does, via the OS trust
store). Work around it by telling the JVM to use the Windows certificate store:

```
$env:GRADLE_OPTS = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
./gradlew build
```

This was required to bootstrap the Gradle wrapper and resolve dependencies in
this environment.

## Verification

- `./gradlew compileJava` — compile main sources
- `./gradlew test` — run unit/integration tests
- `./gradlew build` — full build with tests

## Tests

- Unit tests (Mockito, no Docker required): `./gradlew test --tests "com.healthcareai.service.*" --tests "com.healthcareai.security.*" --tests "com.healthcareai.ai.*"`
- Integration tests (`AbstractIntegrationTest` subclasses) use Testcontainers to spin up a real `pgvector/pgvector:pg16` PostgreSQL container and run Flyway migrations against it. They require a working Docker daemon.

### Known local environment issue

In this sandbox, Docker Desktop 29.x's named-pipe `/info` endpoint returns a
response that `docker-java`/Testcontainers rejects (`BadRequestException`,
status 400), so the Testcontainers-based integration tests
(`AiAutomationApplicationTests`, `AppointmentControllerIntegrationTest`,
`ChatControllerIntegrationTest`) fail to start a container here even though
`docker info`/`docker pull`/`docker compose config` all work fine from the
shell. This reproduced with and without explicit `DOCKER_HOST`/
`DOCKER_API_VERSION` overrides and a `~/.testcontainers.properties` pointing
at the `desktop-linux` pipe. It appears to be a compatibility gap between
this specific Docker Desktop build and the Testcontainers version pulled in
by Spring Boot 3.3.5's dependency management, not a defect in the project
code. These tests should run normally on standard Docker installations
(Linux Docker Engine, GitHub Actions runners, most Docker Desktop versions).
If you hit the same error locally, try upgrading/downgrading Docker Desktop
or the `org.testcontainers:*` versions.

## Module build order

Per project requirements, the codebase is built and verified one module at a
time: Gradle build -> project structure -> configuration -> database ->
entities -> repositories -> services -> AI layer -> tool calling ->
integrations -> controllers -> security -> Docker -> tests.

## Demo data seeding

`com.healthcareai.seed.DemoDataSeeder` (a `CommandLineRunner`) populates the
database with realistic demo data on startup — 15 doctors, 200 patients, 500
appointments (spread across the last/next 30 days, statuses BOOKED
(`SCHEDULED`)/COMPLETED/CANCELLED/RESCHEDULED), 100 knowledge base articles,
and 50 FAQ records — using DataFaker (`net.datafaker:datafaker`) with the
`en-IN` locale for realistic Indian names/addresses. It only inserts into a
table when that table is empty, so it's idempotent and safe to leave enabled
across restarts (verified: a second startup skips all seeding with identical
row counts). Disable with `SEED_DEMO_DATA=false` / `app.seed.enabled=false`
(already disabled in the `test` profile). Counts are configurable via
`app.seed.*` / `SEED_DOCTOR_COUNT`, `SEED_PATIENT_COUNT`,
`SEED_APPOINTMENT_COUNT`, `SEED_KB_ARTICLE_COUNT`, `SEED_FAQ_COUNT`.

Schema additions in `V9__add_doctor_schedule_patient_address_appointment_fee.sql`:
doctor working hours/days, patient address, and an appointment
`consultation_fee` used to compute realized revenue
(`AppointmentRepository.sumConsultationFeeByStatus(COMPLETED)`) — logged by
the seeder at the end of its run.

Knowledge base documents are seeded with `embedding = null` (no Gemini API
calls at startup); run the existing knowledge base ingestion/upsert API
afterwards if semantic RAG search over the seeded content is needed.

## Frontend

A production-oriented React + TypeScript + MUI v6 frontend lives in
`frontend/` (Vite, TanStack Query, React Hook Form + Zod, Recharts). See
`frontend/README.md` for setup, scripts, and a table of which features use
the real backend API vs. a client-side local overlay/mock (for endpoints the
backend doesn't expose yet, e.g. Patient/Doctor CRUD, Knowledge Base,
Settings, dashboard analytics).

```
cd frontend
npm install
npm run dev      # http://localhost:5173, proxies /api to localhost:8081
npm run build    # tsc -b && vite build
npm run lint      # oxlint
```
