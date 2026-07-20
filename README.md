# DispatchHub

### Ride dispatch, driver matching, and trip operations — rebuilt with a clearer technical foundation and a premium enterprise interface.

![DispatchHub trip-management illustration](frontend/public/assets/trips/trip-journey.png)

> DispatchHub models the complete ride lifecycle for **riders**, **drivers**, and
> **administrators**. This repository began as a partially completed Round-2
> hackathon challenge containing intentional defects, missing features, and a
> basic interface. Our work focused on removing the Angular initialization
> blockers and creating one consistent graphite-and-amber product experience.

---

## Project at a glance

| Area | Technology |
|---|---|
| Frontend | Angular 20, standalone components, Angular Material, Signals, RxJS |
| Backend | Java 21, Spring Boot 3.3, Spring Security, JWT, Spring Data JPA |
| Database | PostgreSQL |
| Architecture | Controller → Service → Repository → Entity |
| User roles | Rider, Driver, Administrator |
| Visual system | Graphite `#171717`, Amber `#FFB800`, warm neutral surfaces |

### Core trip lifecycle

```text
REQUESTED → ACCEPTED → ARRIVED → IN_PROGRESS → COMPLETED
     └──────────────── CANCELLED ────────────────┘
```

- **Riders** request rides, review fare estimates, and track trip status.
- **Drivers** manage availability and progress assigned trips.
- **Administrators** monitor trips, drivers, statistics, and operational state.

---

## What we improved

### 1. Removed five Angular initialization failures

Five standalone components produced the TypeScript error:

```text
TS2729: Property is used before its initialization
```

The affected components were:

1. Login
2. Registration
3. Dashboard
4. Main layout
5. Request ride

#### What caused the error?

The components created fields such as forms or signals using a service declared
through a constructor parameter. TypeScript evaluated the field before that
service was guaranteed to be initialized.

```ts
// Before: the form may run before fb is ready.
constructor(private fb: FormBuilder) {}

readonly form = this.fb.group({
  email: [''],
  password: ['']
});
```

#### How we fixed it

Dependencies are now declared with Angular's `inject()` API before any field
that uses them.

```ts
// After: the dependency exists before the form is created.
private readonly fb = inject(FormBuilder);

readonly form = this.fb.group({
  email: [''],
  password: ['']
});
```

This preserves the original behavior while making the initialization order
explicit and safe.

### 2. Diagnosed the Angular and TypeScript compatibility problem

The Angular 20.3 build toolchain expects a TypeScript release in its supported
range, while this checkout currently pins TypeScript `~5.6.0`.

```text
Angular build tools 20.3.x
          ↓ require
TypeScript >= 5.8.0 and < 6.0.0
```

Before performing a clean frontend installation, align the TypeScript pin with
the installed Angular toolchain and regenerate the lockfile. Do not solve this
error by suppressing compiler checks—the dependency versions must agree.

### 3. Unified the entire frontend experience

The application already contained working routes and backend integrations. We
redesigned only the presentation layer and kept the existing behavior intact.

| Experience | Improvements |
|---|---|
| Authentication | Premium split layouts, original route artwork, stronger hierarchy, responsive forms |
| Navigation | Role-aware graphite sidebar, amber active states, live-system indicators, mobile navigation |
| Dashboard | Clear statistics hierarchy, command-center illustration, improved loading and empty states |
| Trip management | Operations ledger, filters, route-focused detail view, timeline, responsive status system |
| Ride request | Polished route form, fare-summary panel, submission and validation states |
| Account toolbar | Correct avatar/name alignment, unclipped role label, responsive account control |

---

## Visual transformation

<table>
  <tr>
    <td width="50%">
      <img src="frontend/public/assets/auth/dispatch-route-illustration.png" alt="DispatchHub authentication route illustration" />
      <br /><strong>Authentication</strong><br />A welcoming entry point for riders, drivers, and administrators.
    </td>
    <td width="50%">
      <img src="frontend/public/assets/dashboard/dispatch-command-center.png" alt="DispatchHub command center illustration" />
      <br /><strong>Dashboard</strong><br />A focused operations surface for live application statistics.
    </td>
  </tr>
  <tr>
    <td width="50%">
      <img src="frontend/public/assets/navigation/route-network.png" alt="DispatchHub route network illustration" />
      <br /><strong>Navigation</strong><br />Role-aware wayfinding with clear state and system feedback.
    </td>
    <td width="50%">
      <img src="frontend/public/assets/trips/trip-journey.png" alt="DispatchHub trip journey illustration" />
      <br /><strong>Trip management</strong><br />A consistent journey language from request to completion.
    </td>
  </tr>
</table>

### Design principles

- **One accent:** amber highlights actions, routes, and active states.
- **Strong hierarchy:** graphite creates dependable enterprise contrast.
- **Responsive by default:** layouts adapt across mobile, tablet, and desktop.
- **Accessible interaction:** visible focus states, keyboard-friendly controls,
  semantic labels, and reduced-motion fallbacks.
- **Purposeful animation:** entrance, hover, skeleton, timeline, and status motion
  support understanding without distracting the user.
- **Original imagery:** project illustrations were generated specifically for
  DispatchHub and contain no third-party brand assets.

---

## Scope guardrails we maintained

The UI modernization intentionally did **not** change:

- Backend services or business rules
- REST API contracts
- Routing behavior
- Form submission logic
- Existing validation rules
- Authentication or JWT integration
- Database integration

Angular Material remains the component foundation. The new visual language is
implemented through semantic HTML, modular SCSS, and native CSS animation.

---

## Architecture

```text
Angular SPA
   │
   │  HTTP + JWT
   ▼
Spring Boot REST API
   │
   ├── Controller
   ├── Service
   ├── Repository
   └── Entity / DTO
          │
          ▼
      PostgreSQL
```

### Repository structure

```text
ride_dispatch/
├── backend/                 Spring Boot API
├── frontend/                Angular application
│   └── public/assets/       DispatchHub illustrations
├── docs/                    API, schema, ERD, and structure documentation
├── seed-data/               Development seed data
├── outputs/                 Project presentation
├── AGENTS.md                Engineering tracker and working context
└── README.md                Project overview
```

Detailed references:

- [API documentation](docs/API-DOCUMENTATION.md)
- [Database schema](docs/DATABASE-SCHEMA.sql)
- [Entity relationship diagram](docs/ER-DIAGRAM.md)
- [Folder structure](docs/FOLDER-STRUCTURE.md)
- [Engineering tracker](AGENTS.md)
- [Project transformation presentation](outputs/DispatchHub_UI_and_Error_Fixes.pptx)

---

## Getting started

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- npm 10+
- PostgreSQL 15+

### 1. Clone the repository

```bash
git clone https://github.com/CredxColab/ride_dispatch.git
cd ride_dispatch
```

### 2. Create the PostgreSQL database

```sql
CREATE DATABASE dispatchhub;
CREATE USER dispatchhub WITH PASSWORD 'dispatchhub';
GRANT ALL PRIVILEGES ON DATABASE dispatchhub TO dispatchhub;
```

PostgreSQL 15 and later may also require:

```sql
\c dispatchhub
GRANT ALL ON SCHEMA public TO dispatchhub;
```

Apply the supplied schema if required:

```bash
psql -U dispatchhub -d dispatchhub -f docs/DATABASE-SCHEMA.sql
```

### 3. Configure the backend

Copy the example environment file:

```bash
cp backend/application-example.env backend/.env
```

Important environment variables:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/dispatchhub` |
| `DB_USERNAME` | `dispatchhub` |
| `DB_PASSWORD` | `dispatchhub` |
| `JWT_EXPIRATION_MS` | `86400000` |
| `JWT_ISSUER` | `dispatchhub-api` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |

Use a strong environment-specific `JWT_SECRET` outside local development.

### 4. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

Health check:

```text
GET http://localhost:8080/actuator/health
```

### 5. Start the frontend

First ensure the TypeScript version is compatible with the installed Angular
20 build tooling. Then install dependencies and start the development server:

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200`.

### Useful commands

```bash
# Frontend development server
npm start

# Frontend production build
npm run build

# Frontend tests
npm test

# Backend tests/package
mvn clean package
```

---

## Features currently available

### Backend

- JWT registration and login
- Role-based authorization
- Trip request and lifecycle operations
- Fare estimation
- Driver availability and location updates
- Paginated trip and driver endpoints
- Dashboard statistics and driver analytics
- Consistent global API error responses
- Development data seeding

### Frontend

- Login and registration connected to the backend
- Authentication, guest, and role guards
- Role-aware application navigation
- Administrator statistics dashboard
- Paginated and filtered trip ledger
- Trip detail and status timeline
- Driver management table
- Ride-request form and fare preview surface

---

## Known incomplete work

The repository is still a challenge codebase. A beautiful interface does not
mean every business feature is complete.

- Rider trip history still displays a presentation-only placeholder.
- Driver incoming requests still use a placeholder screen.
- Nearby-driver search is not implemented and returns `501`.
- Driver review submission is not implemented and returns `501`.
- Administrator force-cancel/reassignment is not implemented.
- Fare estimates still use placeholder coordinates rather than geocoding.
- Trip detail uses interval polling rather than WebSocket or SSE updates.

Security and correctness findings—including trip authorization, cancellation
ownership, public administrator registration, unsafe production defaults, race
conditions, and fare validation—are documented in [AGENTS.md](AGENTS.md). They
should be addressed before a production deployment.

---

## Verification philosophy

Every change should be evaluated against the original hackathon criteria:

1. **Correctness** — fix the underlying cause, not only the visible symptom.
2. **Code quality** — follow the existing Angular and Spring architecture.
3. **Completeness** — clearly distinguish finished work from placeholders.
4. **UI/UX judgment** — maintain one coherent product language.
5. **Verification** — build, test, and manually inspect the affected flow.
6. **Communication** — explain each fix in language a teammate can repeat.

---

## Project story in one sentence

**We stabilized the Angular component initialization flow and transformed a
functional but fragmented dispatch application into a clear, responsive, and
presentation-ready enterprise experience—without changing its business logic.**
