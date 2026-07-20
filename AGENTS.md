# AGENTS.md — DispatchHub (Round 2 Hackathon)

This file is context for Claude Code (or any AI coding agent) working in this
repository. Read this file in full before doing anything else.

---

## Project context

This is a **Round-2 hackathon challenge codebase** (CredX hackathon). Round 1
was a separate project (student job matching dashboard, MERN stack) that got
this team selected into Round 2.

This repo — **DispatchHub**, a ride-hailing dispatch system — is a
partially-built app (~70% complete) handed to us with **intentional bugs,
security issues, and missing features** that we're scored on finding, fixing,
and extending within a fixed time box.

**Stack:**
- Backend: Java 21, Spring Boot 3.3, Maven, Spring Security + JWT, Spring
  Data JPA, PostgreSQL. Layered `Controller → Service → Repository → Entity`.
- Frontend: Angular 20+, standalone components (no NgModules), Angular
  Material, RxJS + HttpClient, Angular Signals, functional route guards.
- Database: PostgreSQL. Schema in `docs/DATABASE-SCHEMA.sql`.

**Evaluation criteria** (from the project README — keep these in mind for
every fix):
- Correctness — real root-cause fixes, not symptom patches
- Code quality — fits existing architecture/conventions, doesn't look bolted on
- Completeness — how much got done in the time available
- UI/UX judgment — frontend work should look/feel consistent with the rest of the app
- Testing/verification — did we check the fix actually works
- Communication — clear commit messages / PR description of what changed and why

## About me (the developer working with you)

- Comfortable in Java/Maven; this is my first Spring Boot codebase specifically.
- Know MySQL, not previously PostgreSQL specifically (mostly fine, JPA abstracts most of it).
- **Do not know Angular** — this is new to me. Explain frontend fixes clearly, don't assume I know Angular idioms.
- I need to be able to explain every change myself afterward (to teammates, in a write-up, or verbally to judges) — so explanations matter as much as the fix itself.

---

## Required workflow — READ CAREFULLY

**Do not fix bugs as you find them. Do not batch multiple fixes together.
Follow this exact sequence:**

### Phase 1 — Discovery only (no code changes)
1. Read `README.md` in full — especially "Known Bugs," "Missing Features,"
   and "Enhancement Opportunities."
2. Read `docs/API-DOCUMENTATION.md`, `docs/ER-DIAGRAM.md`,
   `docs/FOLDER-STRUCTURE.md`.
3. Scan the codebase for existing conventions (how services are injected,
   how DTOs are structured, error handling patterns, naming conventions) so
   future fixes match the existing style.
4. Compile a full list of every known bug, missing feature, and stub you can
   find — from the README AND from your own inspection of the code (things
   the README doesn't mention count too).
5. Write that list into the **"Bug & Feature Tracker"** section below,
   replacing the placeholder. Do not start fixing anything yet.

### Phase 2 — Prioritize
6. Order the tracker list by priority (fastest/highest-impact first), and
   note your reasoning briefly for the ordering.
7. Show me the prioritized list and **wait for my go-ahead** before touching
   any code.

### Phase 3 — Fix, one at a time
8. Work through the tracker **one item at a time**, in order, unless I say
   otherwise.
9. For each item:
   - Show me the relevant current code.
   - Explain what's actually wrong (root cause, not just the symptom).
   - Propose the fix.
   - **Wait for my confirmation before applying it.**
10. After I confirm and you apply a fix:
    - Explain in plain terms what changed and why — written like a commit
      message, clear enough that I could explain it to a teammate or judge.
    - Mark that item's checkbox `[x]` in the tracker below and note the date/commit.
    - Flag if it's a genuine security issue (auth, input validation, exposed
      secrets, etc.) and explain the risk plainly, separate from the fix explanation.
11. Move to the next item only after the current one is confirmed done.

### Never do this
- Don't fix multiple unrelated things in one change.
- Don't silently "clean up" code beyond the scope of the item you're fixing.
- Don't apply a fix without me confirming it first.
- Don't skip the plain-language explanation step.

---

## Bug & Feature Tracker

*(To be filled in during Phase 1. Keep this section updated as the source of
truth for progress — check items off as they're completed, don't delete
finished items.)*

### Backend
- [ ] Driver trip acceptance has a check-then-act race: concurrent accepts can assign one AVAILABLE driver to multiple trips despite the entity's unused `@Version` field.
- [ ] `cancelTrip` does not use `requesterUserId` to verify ownership/assignment; this is tracked as a security issue below.
- [ ] `GET /api/trips/{id}` returns any trip to any authenticated user; this is tracked as a security issue below.
- [ ] `GET /api/trips/driver/{driverProfileId}` permits a DRIVER to retrieve another driver's trip list; this is tracked as a security issue below.
- [ ] `CompleteTripRequest.finalFare` has no validation, so an assigned driver can submit a negative or otherwise arbitrary fare.
- [ ] Driver availability permits an `ON_TRIP` driver to switch to `OFFLINE` (only `ON_TRIP -> AVAILABLE` is blocked), allowing a trip/driver-state inconsistency.
- [ ] Trip and driver paginated list mappings trigger N+1 lazy-load queries; the analytics endpoint also loads an unbounded period's trip rows and aggregates in memory. Performance/scalability issue.
- [ ] README's table of contents and API documentation refer to a “Known Bugs” section, but that section is absent from the README; discovery must therefore rely on source inspection too.

### Frontend
- [x] **Build-blocker:** TS2729 in `login`, `register`, `dashboard`, `main-layout`, and `request-ride` was fixed by replacing constructor parameter-property injection with `inject()` fields declared before dependent class fields. Source reviewed on 2026-07-20; the agent shell has no `npm` executable, so build verification must use the already-running frontend watcher or a developer shell. Commit: `a922183`.
- [ ] Request-ride fare preview is requested only once in `ngOnInit`; form changes never refresh it. With fixed placeholder coordinates, address edits cannot affect the estimate at all.
- [ ] Request-ride marks pickup required but leaves dropoff optional, while the backend requires a nonblank dropoff address. Its error path also leaves the spinner active and displays no error after a failed submission.
- [ ] Rider trip-history route is a placeholder and does not call the already-available `TripService.getMyTrips` endpoint.
- [ ] Driver incoming-requests route is a placeholder; it has no availability/location-aware request retrieval, polling, or accept UI.

### Security issues found
- [ ] **Critical — privilege escalation:** public registration accepts any `UserRole`, including `ADMIN`; a caller can register directly through the API as an administrator and receive an admin JWT.
- [ ] **High — insecure direct object reference:** any authenticated account can fetch any trip by predictable ID, exposing rider/driver names, phone-adjacent trip data, addresses, coordinates, fares, and status history.
- [ ] **High — unauthorized state change:** any RIDER, DRIVER, or ADMIN can cancel any non-terminal trip because cancellation has no caller-role/ownership/assignment check.
- [ ] **High — driver data exposure:** a DRIVER may request another driver's trip history via `/api/trips/driver/{driverProfileId}`; the controller does not compare the path ID with the caller's profile.
- [ ] **High in deployed environments — unsafe defaults:** `application.yml` embeds a default JWT signing secret and enables SQL/application DEBUG logging by default. If environment overrides are missed, tokens can be forged and operational/PII data may be logged.

### Missing features / stubs
- [ ] Nearby available-driver search: `DriverService.findNearbyAvailableDrivers` throws `UnsupportedOperationException`; `GET /api/drivers/nearby` returns 501 and currently has a `Void` response rather than driver results.
- [ ] Rider-to-driver review submission: `ReviewService.submitReview` throws `UnsupportedOperationException`; `POST /api/trips/{id}/review` returns 501. It still needs authorization/state/duplicate checks, persistence, rating aggregation, and response mapping.
- [ ] Admin stuck-trip recovery: `POST /api/admin/trips/{id}/force-cancel` throws `UnsupportedOperationException`; force-cancel/reassignment behavior is absent.
- [ ] Real-time trip updates are not implemented; `TripDetailComponent` deliberately uses interval polling (enhancement opportunity, not a correctness defect).
- [ ] Request-ride geocoding is not implemented; typed addresses always use fixed San Francisco coordinates (enhancement opportunity).
- [ ] Rate limiting, refresh tokens, driver earnings/payouts, surge pricing, integration/contract tests for the trip state machine, and Dockerized full-stack startup are listed enhancement opportunities and are absent.

### Already fixed (before this file was created)
- [x] Angular components using constructor-injected services inside field
      initializers (`this.fb`, `this.authService` used before init — TS2729
      errors). Fixed by switching from constructor injection to `inject()`.
      Confirmed in: `login.component.ts`, `register.component.ts`,
      `dashboard.component.ts`, `main-layout.component.ts`,
      `request-ride.component.ts`. **Verify no other files have this same
      pattern during discovery.**
- [x] `frontend/package.json` had `typescript: ~5.6.0` pinned, incompatible
      with `@angular-devkit/build-angular@20.3.x` which requires TypeScript
      `>=5.8.0 <6.0.0`. Fixed by bumping the pin to `~5.8.0` and reinstalling
      clean (`node_modules` + `package-lock.json` removed and reinstalled).

---

## Environment notes (for reference, not for you to redo)

- Local Postgres 18 required an explicit `GRANT ALL ON SCHEMA public TO dispatchhub;`
  after user creation — Postgres 15+ default privilege changes mean
  `GRANT ALL PRIVILEGES ON DATABASE` alone isn't enough for a new role to
  create tables.
- Seeded admin login is `admin@dispatchhub.com` / `Admin123!` — note this
  differs from the `Password123!` example in the README.
- Backend runs on `localhost:8080`, frontend on `localhost:4200`.

---

## July 2026 stabilization and UI modernization record

Use this section as the concise source of truth when explaining the work to a
teammate, reviewer, or hackathon judge.

### Problems encountered

- Angular 20 tooling and the pinned TypeScript 5.6 version were incompatible.
- Five standalone Angular components produced `TS2729` initialization errors
  because constructor-injected services were referenced by field initializers
  before those services were initialized.
- Authentication, navigation, dashboard, and trip-management screens worked,
  but their visual language was inconsistent and looked like a basic admin
  template.
- The toolbar account control could overflow vertically, clipping the avatar,
  name, role, and dropdown in the upper-right corner.

### How the issues were solved

- Updated the frontend TypeScript version to the range supported by Angular 20.
- Replaced the five unsafe constructor parameter-property usages with Angular's
  `inject()` API, declaring dependencies before fields that use them.
- Preserved every API, route, form, validation rule, service, and business-flow
  binding while redesigning presentation-only HTML and SCSS.
- Introduced a shared graphite (`#171717`) and amber (`#FFB800`) visual language,
  generated original ride-dispatch artwork, responsive layouts, accessible focus
  states, skeletons, empty states, and reduced-motion fallbacks.
- Corrected the Material toolbar button's internal flex alignment and responsive
  sizing so account information remains centered and unclipped.

### Surfaces modernized

- Login, registration, and guest-access presentation.
- Role-aware navigation for rider, driver, and administrator workspaces.
- Role-based dashboard and application statistics.
- Administrator trip list, trip detail, rider request form, trip-history empty
  state, and trip-status chips.

### Guardrails maintained

- No backend behavior or database integration was changed for the UI work.
- No frontend service, API, routing, validation, or business logic was changed
  during the redesign.
- Angular Material remains the single component foundation; enhancements use
  modular SCSS and native CSS motion.
