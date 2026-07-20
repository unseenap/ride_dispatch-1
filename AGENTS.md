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
- [x] Driver trip acceptance had a check-then-act race: concurrent accepts could assign one AVAILABLE driver to multiple trips. Fixed by pessimistically locking both the trip and driver profile before checking/updating their states. Verified by successful `mvn test` compilation on 2026-07-20. Commit: `9f2e7c2`.
- [x] `cancelTrip` does not use `requesterUserId` to verify ownership/assignment; this is tracked as a security issue below. Fixed there — commit `5322599`.
- [x] `GET /api/trips/{id}` returns any trip to any authenticated user; this is tracked as a security issue below. Fixed there — commit `be8378f`.
- [x] `GET /api/trips/driver/{driverProfileId}` permits a DRIVER to retrieve another driver's trip list; this is tracked as a security issue below. Fixed there — commit `7469984`.
- [x] `CompleteTripRequest.finalFare` lacked validation, so an assigned driver could submit a negative or database-incompatible fare. Fixed with non-negative and `numeric(10,2)`-compatible Bean Validation constraints; controller now applies validation. Verified by successful `mvn test` compilation on 2026-07-20. Commit: `6403974`.
- [x] Driver availability allowed an `ON_TRIP` driver to switch to `OFFLINE`, creating a trip/driver-state inconsistency. Fixed by making `ON_TRIP` system-managed: drivers cannot manually enter or leave it. Commit: `4891a42`.
- [x] Trip and driver paginated list mappings trigger N+1 lazy-load queries; the analytics endpoint also loads an unbounded period's trip rows and aggregates in memory. Fixed with `@EntityGraph` on the paginated finders, `@BatchSize` on trip status history, and a JPQL group-by aggregate replacing the in-memory trips-per-driver loop. Verified by successful `mvn compile` on 2026-07-20. Commit: `0ce96a9`.
- [x] README's table of contents and API documentation refer to a “Known Bugs” section, but that section is absent from the README; discovery must therefore rely on source inspection too. Informational only — discovery was done via full source inspection; no code change needed. Noted 2026-07-20.

- [x] **Found during fixing (not in tracker):** `GeoUtils.distanceKm` applied the haversine formula to raw degrees instead of radians, so every computed distance/fare/duration was wildly wrong. Fixed with `Math.toRadians` conversions. Verified by successful `mvn compile` on 2026-07-20. Commit: `a523f54`.

### Frontend
- [x] **Build-blocker:** TS2729 in `login`, `register`, `dashboard`, `main-layout`, and `request-ride` was fixed by replacing constructor parameter-property injection with `inject()` fields declared before dependent class fields. Source reviewed on 2026-07-20; the agent shell has no `npm` executable, so build verification must use the already-running frontend watcher or a developer shell. Commit: `a922183`.
- [ ] Request-ride fare preview is requested only once in `ngOnInit`; form changes never refresh it. With fixed placeholder coordinates, address edits cannot affect the estimate at all.
- [ ] Request-ride marks pickup required but leaves dropoff optional, while the backend requires a nonblank dropoff address. Its error path also leaves the spinner active and displays no error after a failed submission.
- [ ] Rider trip-history route is a placeholder and does not call the already-available `TripService.getMyTrips` endpoint.
- [ ] Driver incoming-requests route is a placeholder; it has no availability/location-aware request retrieval, polling, or accept UI.

### Security issues found
- [x] **Critical — privilege escalation:** public registration accepted any `UserRole`, including `ADMIN`. Fixed by allowing only `RIDER` and `DRIVER` in `AuthService.register`; `ADMIN` now receives a 400 response. Verified by successful `mvn test` compilation on 2026-07-20. Commit: `55ad544`.
- [x] **High — insecure direct object reference:** any authenticated account could fetch any trip by predictable ID, exposing rider/driver names, phone-adjacent trip data, addresses, coordinates, fares, and status history. Fixed in `TripService`: only the rider, assigned driver, or admin can view a trip; other callers receive 403. Verified by successful `mvn test` compilation on 2026-07-20. Commit: `be8378f`.
- [x] **High — unauthorized state change:** any RIDER, DRIVER, or ADMIN could cancel any non-terminal trip because cancellation had no caller-role/ownership/assignment check. Fixed in `TripService`: only the owning rider, assigned driver, or an admin can cancel; other callers receive 403. Verified by successful `mvn test` compilation on 2026-07-20. Commit: `5322599`.
- [x] **High — driver data exposure:** a DRIVER could request another driver's trip history via `/api/trips/driver/{driverProfileId}`. Fixed in `TripService`: a driver may request only their own profile ID, while admins retain access; other drivers receive 403. Verified by successful `mvn test` compilation on 2026-07-20. Commit: `7469984`.
- [x] **High in deployed environments — unsafe defaults:** `application.yml` embeds a default JWT signing secret and enables SQL/application DEBUG logging by default. If environment overrides are missed, tokens can be forged and operational/PII data may be logged. Fixed 2026-07-20: base config has no `JWT_SECRET` fallback (fail-fast) and defaults to INFO/no show-sql; a `dev` profile restores the old local-dev behavior (`-Dspring-boot.run.profiles=dev`). README updated. Commit: `2319e3c`.

### Missing features / stubs
- [x] Nearby available-driver search: `DriverService.findNearbyAvailableDrivers` throws `UnsupportedOperationException`; `GET /api/drivers/nearby` returns 501 and currently has a `Void` response rather than driver results. Implemented 2026-07-20 with a bounding-box repository query + haversine filter, nearest-first sort, 20-result cap, and radius validation; API docs updated. Verified by successful `mvn compile`. Commit: `c3d773c`.
- [x] Rider-to-driver review submission: `ReviewService.submitReview` throws `UnsupportedOperationException`; `POST /api/trips/{id}/review` returns 501. Implemented 2026-07-20 with ownership (403), COMPLETED-state and duplicate (409) checks, persistence, driver average-rating recomputation, and `ReviewResponse` mapping; API docs updated. Verified by successful `mvn compile`. Commit: `1970044`.
- [x] Admin stuck-trip recovery: `POST /api/admin/trips/{id}/force-cancel` throws `UnsupportedOperationException`; force-cancel/reassignment behavior is absent. Implemented 2026-07-20 as `TripService.forceCancelTrip`: locks the trip row, rejects terminal states with 409, transitions to CANCELLED with a history note (optional reason body), and returns an ON_TRIP driver to AVAILABLE; API docs updated. Verified by successful `mvn compile`. Commit: `a9aed96`.
- [ ] Real-time trip updates are not implemented; `TripDetailComponent` deliberately uses interval polling (enhancement opportunity, not a correctness defect).
  - [x] Backend half done 2026-07-20: `GET /api/trips/{id}/events` streams SSE `trip-update` events — snapshot on subscribe, one event per transition published after commit via `TripEventPublisher`, auto-close on terminal states; same access rule as trip detail; API docs updated (incl. EventSource/JWT caveat). `mvn test` green. Frontend consumption can replace `TripDetailComponent` polling later. Commit: `f151e7a`.
- [ ] Request-ride geocoding is not implemented; typed addresses always use fixed San Francisco coordinates (enhancement opportunity).
  - [x] Backend half done 2026-07-20: `GET /api/geo/geocode?q=` proxies a Nominatim-compatible provider (default OSM, `GEOCODING_BASE_URL` overridable) with in-memory caching; API docs updated; `mvn test` still green. Frontend wiring happens with the request-ride frontend fixes. Commit: `381e040`.
- [ ] Rate limiting, refresh tokens, driver earnings/payouts, surge pricing, integration/contract tests for the trip state machine, and Dockerized full-stack startup are listed enhancement opportunities and are absent.
  - [x] Rate limiting: implemented 2026-07-20 as a per-IP token-bucket `RateLimitFilter` on `/api/auth/**` (burst 10, 10/min sustained, configurable via `dispatchhub.rate-limit.*`; 429 with the standard error shape); API docs updated. Verified by successful `mvn compile`. Commit: `e2d61ae`.
  - [x] Refresh tokens: implemented 2026-07-20 — opaque single-use tokens (SHA-256 hashed at rest, 30-day default) with rotation on `/api/auth/refresh`, reuse-detection revoking the whole token family, and `/api/auth/logout` revoking all of the caller's tokens; API docs updated. Verified by successful `mvn compile`. Commit: `b83293c`.
  - [x] Driver earnings: implemented 2026-07-20 as `GET /api/drivers/me/earnings` — DB-side aggregate (completed trips, total earnings via finalFare-else-estimate, average fare, distance) over an optional `from`/`to` window defaulting to the last 30 days; API docs updated. Verified by successful `mvn compile`. Commit: `403fa08`.
  - [x] Surge pricing: implemented 2026-07-20 as `SurgePricingService` — multiplier scales linearly from the base (1.0) to `max-surge-multiplier` (default 2.5) with the ratio of REQUESTED trips to AVAILABLE drivers; applied in fare estimates and trip-request fares, and returned as `surgeMultiplier` in the estimate response; API docs updated. Verified by successful `mvn compile`. Commit: `4bc889b`.
  - [x] Trip state-machine tests: added 2026-07-20 — `TripStateMachineIntegrationTest` (12 tests) running the real services against in-memory H2 (`application-test.yml` test profile): full happy path with driver-status side effects and history ordering, invalid-transition rejections, ownership checks, cancel/force-cancel behavior, and nearby-driver search. All 12 pass via `mvn test`; README updated. Commit: `ca65208`.
  - [x] Dockerized full-stack startup: added 2026-07-20 — multi-stage `backend/Dockerfile` (Maven build → JRE 21 alpine, non-root), `frontend/Dockerfile` (Node 22 build → nginx serving the prod bundle and proxying `/api`), and root `docker-compose.yml` (Postgres 16 with healthcheck, backend, frontend; dev profile default, `JWT_SECRET`/`SPRING_PROFILES_ACTIVE` overridable); README updated. NOT build-verified: Docker is not installed in the agent shell — verify with `docker compose up --build`. Commit: `8df55c8`.

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
