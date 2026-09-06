# HealthcareAI Console (Frontend)

Production-oriented React + TypeScript + Material UI frontend for the
HealthcareAI Spring Boot backend.

## Stack

- React 18/19 + TypeScript (strict) + Vite
- Material UI v6, MUI X Data Grid, MUI X Date Pickers
- React Router v6
- TanStack React Query v5
- React Hook Form + Zod
- Axios (with JWT interceptor)
- Recharts
- Zustand (auth/theme state, persisted to localStorage)
- react-markdown + react-syntax-highlighter (AI chat)

## Getting started

```
npm install
cp .env.example .env   # already done for you locally
npm run dev
```

The Vite dev server runs on `http://localhost:5173` and proxies `/api/*`
requests to the Spring Boot backend at `http://localhost:8081` (see
`vite.config.ts`), so no backend CORS configuration is required for local
development. In production, set `VITE_API_BASE_URL` to the deployed API URL.

## Scripts

- `npm run dev` — start the dev server
- `npm run build` — type-check (`tsc -b`) and produce a production build in `dist/`
- `npm run lint` — run oxlint
- `npm run preview` — preview the production build locally

## Auth

Staff log in with email/password against `POST /api/auth/login`. The JWT is
decoded client-side (role, expiry) and persisted in `localStorage` via
Zustand, so a page refresh keeps the session. `AuthWatcher` polls the token
expiry and automatically signs the user out (the backend does not currently
issue refresh tokens).

## Backend API coverage

The backend does not yet expose full CRUD for every resource. Where an
endpoint is missing, this app uses a client-side "local overlay" or mock
service (persisted to `localStorage`) so every page in the spec is fully
functional today. These are intentionally isolated behind small service
modules so they can be deleted once the backend catches up:

| Feature | Real backend endpoint | Status |
| --- | --- | --- |
| Login / register | `POST /api/auth/login`, `/register` | ✅ real |
| Patients: list/get | `GET /api/patients[/{id}]` | ✅ real |
| Patients: create/update/delete/notes | — | 🟡 local overlay (`services/patientService.ts`) |
| Doctors: list/get | `GET /api/doctors[/{id}]` | ✅ real |
| Doctors: create/update/delete/schedule | — | 🟡 local overlay (`services/doctorService.ts`) |
| Appointments: list/create/update/cancel | `/api/appointments` | ✅ real |
| AI Chat | `POST /api/chat` | ✅ real |
| Knowledge Base (FAQs) | — (entity exists, no controller) | 🟡 local mock (`services/knowledgeBaseService.ts`) |
| Settings (clinic/reminders/AI/notifications) | — (`application.yml` only) | 🟡 local mock (`services/settingsService.ts`) |
| Dashboard/Analytics | — | 🟢 computed client-side from real Patients/Doctors/Appointments data (`services/dashboardService.ts`); revenue is a clearly-labeled estimate since there is no billing entity |
| Chat conversation history sidebar | — (Conversation entity has no list endpoint) | 🟡 local mirror (`services/chatHistoryService.ts`); AI replies always come from the real endpoint |

## Project structure

```
src/
  api/          axios client + typed endpoint wrappers for real backend routes
  auth/         ProtectedRoute, auto-logout watcher
  components/   shared UI (common/, charts/)
  hooks/        React Query hooks per domain
  layouts/      app shell (sidebar, top bar, notifications)
  pages/        route-level feature pages
  services/     business logic + local-overlay/mock persistence
  store/        Zustand stores (auth, theme)
  theme/        MUI theme + palettes
  types/        shared TypeScript types/interfaces
  utils/        small helpers
```
