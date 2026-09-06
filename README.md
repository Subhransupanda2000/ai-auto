# AI Automation Platform for Healthcare Clinics

A production-ready Java 21 / Spring Boot 3 backend that powers an **AI Receptionist**
for healthcare clinics: answering patient questions, booking/rescheduling/cancelling
appointments, checking doctor availability, sending reminders and confirmations, and
escalating to a human receptionist when needed. It integrates with Google Gemini, Google
Calendar, WhatsApp Business, Twilio SMS, and email, and exposes a small REST API for
clinic staff and for n8n workflow automation.

This is a backend-only automation service — there is no bundled frontend/UI.

---

## Table of contents

- [Architecture](#architecture)
- [Folder structure](#folder-structure)
- [How the AI works](#how-the-ai-works)
- [How RAG works](#how-rag-works)
- [How Tool Calling works](#how-tool-calling-works)
- [Environment variables](#environment-variables)
- [Running locally](#running-locally)
- [Docker setup](#docker-setup)
- [Testing](#testing)
- [Security](#security)
- [REST API](#rest-api)

---

## Architecture

The project follows a classic layered/clean architecture:

```
Controller (REST)  ->  Service (business logic)  ->  Repository (Spring Data JPA)  ->  PostgreSQL/pgvector
                              ^
                              |
                     AI Agent (ai.agent) ---> Tools (ai.tools) ---> Services / Integrations
                              |
                     Gemini Client (integration.gemini)
```

- **Controllers** are thin: they validate input, delegate to services, and map
  entities to DTOs via mapper classes.
- **Services** hold all business rules (appointment conflict detection, working
  hours, cancellation rules, etc.) and are the single source of truth used by
  both the REST API and the AI agent's tools — there is no duplicated business
  logic between the two entry points.
- **Integrations** (Google Gemini, Google Calendar, WhatsApp, Twilio, Email) are
  each defined behind an interface in `com.healthcareai.integration`, with the
  concrete HTTP-based implementation in a sub-package (Gemini and Google
  Calendar/WhatsApp/Twilio/Email are all called directly via REST — no vendor
  SDKs are used for the AI provider). This makes it easy to swap providers or
  substitute mocks in tests.
- **AI layer** (`com.healthcareai.ai`) orchestrates a chat completion loop with
  tool calling and Retrieval Augmented Generation (RAG) over a pgvector-backed
  knowledge base.
- Every external call that can fail without blocking the core flow (Google
  Calendar sync, notification sending) is designed to degrade gracefully
  (logged and skipped) rather than fail the request.

## Folder structure

```
src/main/java/com/healthcareai/
  config/         Spring configuration: security, web/CORS, OpenAPI, REST clients,
                  async executor, and typed @ConfigurationProperties classes.
  controller/     REST controllers: chat, appointments, patients, doctors, auth.
  service/        Business services: patients, doctors, appointments, conversations,
                  notifications, audit log, users.
  repository/     Spring Data JPA repositories, including the pgvector similarity
                  search queries.
  entity/         JPA entities mapping to the database schema.
  dto/            Request/response DTOs for the REST API.
  mapper/         Entity <-> DTO mapping.
  security/       JWT issuance/validation, UserDetailsService, JWT auth filter.
  exception/      Domain exceptions + the global exception handler.
  integration/    External service interfaces (GeminiClient, WhatsAppService,
                  SmsService, EmailService, CalendarIntegrationService) and their
                  provider-specific implementations (gemini/, whatsapp/, twilio/,
                  email/, googlecalendar/ sub-packages).
  ai/             RAG (KnowledgeBaseService), the chat message model, and:
    ai/agent/     ReceptionistAgent (the orchestration loop) and ToolRegistry.
    ai/tools/     CalendarTool, PatientTool, AppointmentTool, NotificationTool,
                  KnowledgeTool, EscalationTool.
    ai/prompts/   SystemPromptProvider (the reusable system prompt).
  workflow/       n8n webhook integration points (extend here for inbound webhooks).
  scheduler/      Scheduled jobs (appointment reminders).
  util/           Shared utilities.

src/main/resources/
  application.yml           Main configuration (env-var driven).
  db/migration/              Flyway SQL migrations (schema + pgvector setup).

src/test/java/com/healthcareai/
  AbstractIntegrationTest.java   Testcontainers (pgvector/pgvector:pg16) base class.
  service/, security/, ai/       Mockito unit tests.
  controller/                    Full-stack integration tests.
```

## How the AI works

`ReceptionistAgent` (`ai.agent.ReceptionistAgent`) handles one turn of a
conversation:

1. The incoming user message is persisted to the `conversations` table
   (keyed by `sessionId`).
2. The knowledge base is searched (RAG) for content relevant to the message.
3. A system prompt is built (`ai.prompts.SystemPromptProvider`) embedding the
   clinic's rules and the retrieved knowledge.
4. Up to the last 20 USER/ASSISTANT messages for the session are loaded as
   conversation history.
5. `GeminiClient.createChatCompletion(...)` is called with the full message
   list and every registered tool definition. Internally this calls Google
   Gemini's `generateContent` REST API directly (no SDK) at
   `POST {base-url}/models/{model}:generateContent`, authenticated via the
   `x-goog-api-key` header.
6. If the model requests one or more tool calls, `ToolRegistry` executes them
   and their results are appended to the message list; the loop repeats
   (bounded to 5 iterations) until the model returns a plain-text answer.
7. The final answer is persisted and returned to the caller.

If the Gemini call fails after retries (`GeminiClientImpl` is annotated with
`@Retryable`, 3 attempts with exponential backoff), the agent returns a
graceful fallback message and automatically escalates the conversation to a
human receptionist rather than surfacing an error to the patient.

### Provider abstraction

`GeminiClient` (`com.healthcareai.integration.GeminiClient`) is the sole
interface the AI orchestration layer (`ReceptionistAgent`,
`KnowledgeBaseService`) depends on. `GeminiClientImpl`
(`com.healthcareai.integration.gemini`) is the only implementation, and talks
to Gemini's REST API directly using Spring's `RestClient` — there is no
OpenAI, Ollama, or other provider code left in the project. Swapping or
mocking the AI provider only requires providing a different `GeminiClient`
bean (see `ChatControllerIntegrationTest` and `KnowledgeBaseServiceImplTest`
for how it's mocked in tests).

## How RAG works

Clinic knowledge (FAQs, doctor profiles, clinic timings, insurance
information, medical services) is stored in the `faq_documents` table with a
pgvector `vector(1536)` column (`FaqDocument.embedding`), matching Gemini's
`gemini-embedding-001` model truncated to 1536 dimensions (via the
`outputDimensionality` request parameter, one of Google's recommended sizes
alongside 768 and 3072 — no schema migration needed).

- **Ingestion**: `KnowledgeBaseService.upsertDocument(...)` computes an
  embedding for the document's title + content via `GeminiClient.createEmbedding`
  (calling Gemini's `embedContent` REST API) and stores it.
- **Retrieval**: `KnowledgeBaseService.retrieveRelevant(query, n)` embeds the
  query and calls `FaqDocumentRepository.findMostSimilar(...)`, an HQL query
  using Hibernate's `cosine_distance()` function (from the `hibernate-vector`
  module), which is translated into pgvector's native `<=>` operator with an
  HNSW index (see `V7__create_faq_documents_table.sql`) for fast approximate
  nearest-neighbour search.
- The top matches are embedded directly into the system prompt for that turn,
  and the model is instructed never to answer clinic-specific questions
  beyond what's in that context (see the system prompt rules).

There is currently no REST endpoint to manage knowledge base documents from
outside the JVM; use `KnowledgeBaseService` directly (e.g. from a
`CommandLineRunner`, an admin script, or by extending `controller/` with an
admin-only endpoint) to seed FAQs, doctor profiles, clinic timings, insurance
information, and medical services.

## How Tool Calling works

Tools are grouped into `@Component` classes implementing `ai.tools.ToolProvider`,
each contributing one or more `ToolFunction`s (name, description, JSON Schema
parameters, and an executor):

| Tool class | Functions |
|---|---|
| `CalendarTool` | `calendar_checkAvailability`, `calendar_bookAppointment`, `calendar_cancelAppointment`, `calendar_rescheduleAppointment` (keeps Google Calendar in sync) |
| `PatientTool` | `patient_createPatient`, `patient_findPatient`, `patient_updatePatient` |
| `AppointmentTool` | `appointment_createAppointment`, `appointment_findAppointment`, `appointment_updateAppointment`, `appointment_cancelAppointment` |
| `NotificationTool` | `notification_sendWhatsApp`, `notification_sendSms`, `notification_sendEmail` |
| `KnowledgeTool` | `knowledge_searchKnowledgeBase` |
| `EscalationTool` | `conversation_escalateToHuman` |

`ToolRegistry` collects every `ToolProvider` bean in the Spring context into a
single name-addressable map, exposes `allDefinitions()` for the Gemini request
(translated into a single `functionDeclarations` array, per Gemini's function
calling format), and safely executes a requested tool call — any exception (missing argument,
not-found entity, business rule violation) is converted into a JSON error
payload fed back to the model instead of crashing the conversation, so the
model can ask a follow-up question or apologize and escalate.

All tool implementations delegate to the same `service.*` classes used by the
REST controllers — there's a single source of truth for business rules such as
appointment conflict detection.

## Environment variables

See [`.env.example`](.env.example) for the full list with defaults. Key ones:

| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection |
| `JWT_SECRET` | HMAC signing key for staff JWTs (**must** be changed in production) |
| `GEMINI_API_KEY`, `GEMINI_MODEL`, `GEMINI_EMBEDDING_MODEL` | Google Gemini credentials/models (see [Gemini configuration](#gemini-configuration)) |
| `TWILIO_ENABLED`, `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER` | SMS |
| `WHATSAPP_ENABLED`, `WHATSAPP_PHONE_NUMBER_ID`, `WHATSAPP_ACCESS_TOKEN` | WhatsApp Cloud API |
| `GOOGLE_CALENDAR_ENABLED`, `GOOGLE_CALENDAR_ID`, `GOOGLE_CALENDAR_KEY_PATH` | Google Calendar (service account JSON key file path) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP email |
| `N8N_ENABLED`, `N8N_WEBHOOK_BASE_URL`, `N8N_INBOUND_WEBHOOK_SECRET` | n8n workflow automation |
| `CLINIC_NAME`, `CLINIC_TIMEZONE`, `CLINIC_WORKING_HOURS_START/END`, `APPOINTMENT_SLOT_MINUTES`, `REMINDER_HOURS_BEFORE` | Clinic business rules |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed origins (empty = no cross-origin access) |

Every integration is disabled by default (`*_ENABLED=false` / empty
credentials) and degrades gracefully — the app starts and the chat/appointment
flows work even with nothing but a database and a Gemini API key configured.

### Gemini configuration

| Variable | Default | Purpose |
|---|---|---|
| `GEMINI_API_KEY` | *(empty)* | API key from [Google AI Studio](https://aistudio.google.com/apikey). Required for chat and embeddings to work. |
| `GEMINI_MODEL` | `gemini-flash-latest` | Chat/tool-calling model, called via `POST {base-url}/models/{model}:generateContent`. |
| `GEMINI_BASE_URL` | `https://generativelanguage.googleapis.com/v1beta` | Gemini REST API base URL. |
| `GEMINI_TIMEOUT_SECONDS` | `30` | HTTP connect/read timeout for Gemini calls. |
| `GEMINI_TEMPERATURE` | `0.2` | Sampling temperature for chat completions. |
| `GEMINI_EMBEDDING_MODEL` | `gemini-embedding-001` | Embedding model, called via `POST {base-url}/models/{model}:embedContent`. |
| `GEMINI_EMBEDDING_DIMENSIONS` | `1536` | Requested output vector size (via `outputDimensionality`); must match the `faq_documents.embedding` pgvector column. |

These map directly onto `app.gemini.*` in `application.yml` and
`com.healthcareai.config.GeminiProperties`.

> **Model availability varies per API key/project.** Pinned model names like
> `gemini-2.5-flash` and `gemini-2.5-flash-lite` can return HTTP 404
> (`"This model ... is no longer available to new users"`) for keys/projects
> created after Google's cutover date, even though they still appear in the
> `models.list` response. `gemini-flash-latest` is a stable alias Google
> keeps pointed at a currently-available flash model, so it's used as the
> default here. To see exactly which models your key supports, run:
> ```bash
> curl "https://generativelanguage.googleapis.com/v1beta/models?key=$GEMINI_API_KEY"
> ```
> and check `generateContent` calls succeed with `curl -H "x-goog-api-key: $GEMINI_API_KEY" ".../models/<model>:generateContent" -d '{"contents":[{"role":"user","parts":[{"text":"hi"}]}]}'`
> before relying on a specific pinned model name in production.

## Running locally

Prerequisites: JDK 21, and either a local PostgreSQL 16+ with the `pgvector`
extension, or Docker (see below) to run one.

```bash
# One-time: generate the Gradle wrapper jar if it's missing
# (already committed in this repo)

cp .env.example .env    # then edit .env with real values
./gradlew bootRun
```

The app reads configuration from environment variables (see `application.yml`);
export the variables from `.env` into your shell, or use a tool like
`direnv`/`dotenv` before running `bootRun`.

On Windows, if Gradle fails to resolve dependencies with a `PKIX path
building failed` TLS error (common behind corporate proxies), see the note in
[`AGENTS.md`](AGENTS.md).

Once running:
- API base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

The first call to `POST /api/auth/register` creates the bootstrap `ADMIN` user
(see [Security](#security)).

## Docker setup

```bash
cp .env.example .env    # edit as needed; GEMINI_API_KEY at minimum
docker compose up --build
```

This starts:
- `postgres`: `pgvector/pgvector:pg16` with a persisted volume, health-checked.
- `app`: builds the Spring Boot app via the multi-stage `Dockerfile` and waits
  for `postgres` to be healthy before starting. Flyway applies all migrations
  automatically on boot.

To enable Google Calendar sync under Docker, mount your service account key
and set the corresponding environment variables — see the commented-out
example in `docker-compose.yml`.

## Testing

- **Unit tests** (JUnit 5 + Mockito + AssertJ, no external services required):
  `AppointmentServiceImplTest`, `JwtServiceTest`, `ToolRegistryTest`,
  `KnowledgeBaseServiceImplTest`.
- **Integration tests** (JUnit 5 + Spring Boot Test + Testcontainers): boot the
  full application context against a real `pgvector/pgvector:pg16` container
  with Flyway migrations applied, exercising the appointment booking lifecycle
  and the `/api/chat` endpoint (with `GeminiClient` mocked via `@MockBean`).

```bash
./gradlew test
```

Requires a working Docker daemon for the integration tests; see
[`AGENTS.md`](AGENTS.md) for a note on a Docker Desktop compatibility issue
observed in one local environment.

## Security

- Stateless JWT authentication (`io.jsonwebtoken` / jjwt) for clinic staff.
- Roles: `ADMIN`, `DOCTOR`, `RECEPTIONIST` (`entity.Role`), stored on the
  `users` table and encoded as a `role` claim in the JWT.
- `POST /api/chat` and `POST /api/auth/**` are public (patients don't
  authenticate; the chat endpoint is meant to be called directly or proxied
  from WhatsApp/SMS/n8n). Registering the very first user bootstraps them as
  `ADMIN`; every subsequent registration requires an authenticated `ADMIN`.
- `GET` endpoints under `/api/patients`, `/api/doctors`, `/api/appointments`
  require any staff role; `POST`/`PUT`/`DELETE` on `/api/appointments`
  require `ADMIN` or `RECEPTIONIST`.
- Passwords are hashed with BCrypt.
- A global exception handler (`exception.GlobalExceptionHandler`) returns
  consistent JSON error bodies and maps LLM/integration failures to `503`
  instead of leaking stack traces.

## REST API

| Method | Path | Description | Auth |
|---|---|---|---|
| `POST` | `/api/chat` | Send a message to the AI receptionist | Public |
| `POST` | `/api/auth/register` | Register a staff user (bootstrap or ADMIN-only) | Public (self-restricting) |
| `POST` | `/api/auth/login` | Obtain a JWT | Public |
| `GET` | `/api/appointments` | List appointments (filter by `patientId`/`doctorId`) | Staff |
| `POST` | `/api/appointments` | Book an appointment | ADMIN/RECEPTIONIST |
| `PUT` | `/api/appointments/{id}` | Reschedule and/or confirm/complete | ADMIN/RECEPTIONIST |
| `DELETE` | `/api/appointments/{id}` | Cancel an appointment | ADMIN/RECEPTIONIST |
| `GET` | `/api/patients`, `/api/patients/{id}` | List/get patients | Staff |
| `GET` | `/api/doctors`, `/api/doctors/{id}` | List/get doctors (filter by `specialty`) | Staff |

Full OpenAPI documentation is available at `/swagger-ui.html` when the app is
running.

### Sample request: `/api/chat`

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
        "sessionId": "session-123",
        "message": "What are your clinic opening hours?"
      }'
```

Response:

```json
{
  "sessionId": "session-123",
  "reply": "Our clinic is open Monday to Friday, 9:00 AM to 5:00 PM."
}
```

This endpoint is public (see [Security](#security)) and requires
`GEMINI_API_KEY` to be configured server-side; no client-side API key is
needed.
