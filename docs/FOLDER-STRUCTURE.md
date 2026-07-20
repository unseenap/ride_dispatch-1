# DispatchHub — Folder Structure

## Backend (`backend/`)

```
backend/
├── pom.xml                          # Maven build: Spring Boot 3.3.4, Java 21, jjwt, postgresql
├── application-example.env          # Template for local env vars / secrets (copy to .env)
└── src/
    ├── main/
    │   ├── java/com/credx/dispatchhub/
    │   │   ├── DispatchHubApplication.java   # @SpringBootApplication entry point
    │   │   │
    │   │   ├── config/                       # Cross-cutting Spring configuration
    │   │   │   ├── SecurityConfig.java       #   Spring Security filter chain, CORS, password encoder
    │   │   │   ├── FareProperties.java       #   @ConfigurationProperties for fare formula constants
    │   │   │   └── DataLoader.java           #   CommandLineRunner that seeds dev data on empty DB
    │   │   │
    │   │   ├── controller/                   # REST endpoints (thin - delegate to services)
    │   │   │   ├── AuthController.java       #   /api/auth/** (register, login)
    │   │   │   ├── DriverController.java     #   /api/drivers/**
    │   │   │   ├── RiderController.java      #   /api/riders/**
    │   │   │   ├── TripController.java       #   /api/trips/**
    │   │   │   └── AdminController.java      #   /api/admin/** (dashboard stats, analytics)
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── request/                  # Inbound request bodies with Bean Validation annotations
    │   │   │   └── response/                 # Outbound response shapes (never expose entities directly)
    │   │   │
    │   │   ├── entity/                       # JPA entities (User, DriverProfile, RiderProfile,
    │   │   │                                   Trip, TripStatusHistory, Review)
    │   │   │
    │   │   ├── enums/                        # UserRole, DriverStatus, TripStatus
    │   │   │
    │   │   ├── exception/                    # Custom exceptions + GlobalExceptionHandler (@RestControllerAdvice)
    │   │   │
    │   │   ├── repository/                   # Spring Data JPA repositories, one per aggregate root
    │   │   │
    │   │   ├── security/                     # JWT issuing/parsing, UserDetailsService, auth filter,
    │   │   │                                   CurrentUser helper for pulling the authenticated principal
    │   │   │
    │   │   ├── service/                      # Business logic layer (transactional boundaries live here)
    │   │   │   ├── AuthService.java
    │   │   │   ├── DriverService.java
    │   │   │   ├── RiderService.java
    │   │   │   ├── TripService.java          #   trip lifecycle state machine + matching
    │   │   │   ├── FareEstimationService.java
    │   │   │   ├── AnalyticsService.java     #   dashboard stats + trips-per-driver aggregation
    │   │   │   └── ReviewService.java        #   rider->driver rating submission (stub, see TODOs)
    │   │   │
    │   │   └── util/                         # Stateless helpers: GeoUtils (distance), DateTimeUtils
    │   │
    │   └── resources/
    │       └── application.yml               # Spring config, reads secrets from env vars
    │
    └── test/
        └── java/com/credx/dispatchhub/       # Test source root (empty scaffold - add your own tests)
```

**Layering convention**: `Controller -> Service -> Repository -> Entity`, with
DTOs at the controller boundary in both directions. Controllers should not
touch repositories or entities directly; services should not depend on
Spring MVC types.

## Frontend (`frontend/`)

```
frontend/
├── angular.json                     # Angular CLI workspace config (application builder)
├── package.json                     # Angular 20, Angular Material, RxJS
├── tsconfig*.json                   # TypeScript project configs (app / spec / base)
└── src/
    ├── index.html
    ├── main.ts                      # bootstrapApplication(AppComponent, appConfig)
    ├── styles.scss                  # Global styles + shared spacing/status-color custom properties
    │
    ├── environments/
    │   ├── environment.ts           # Dev config (apiBaseUrl, pollingIntervalMs)
    │   └── environment.prod.ts
    │
    └── app/
        ├── app.component.ts         # Root standalone component (just a <router-outlet>)
        ├── app.config.ts            # provideRouter/provideHttpClient/interceptors wiring
        ├── app.routes.ts            # Route table with lazy-loaded standalone components
        │
        ├── core/                    # App-wide singletons, no UI
        │   ├── models/              #   TypeScript interfaces matching backend DTOs
        │   ├── services/            #   AuthService, TripService, DriverService, RiderService,
        │   │                           DashboardService - all HttpClient + RxJS, AuthService also
        │   │                           exposes Angular Signals for current-user state
        │   ├── guards/               #   authGuard, guestGuard, roleGuard - functional CanActivateFn
        │   ├── interceptors/         #   authInterceptor (attaches JWT), errorInterceptor (401 handling)
        │   └── utils/                 #   date-format.util.ts - shared timestamp formatter
        │
        └── features/                 # Route-level feature areas, one folder per page/flow
            ├── auth/
            │   ├── login/
            │   └── register/
            ├── dashboard/             #   Admin stats cards / rider-driver welcome view
            ├── trips/
            │   ├── trip-list/         #   Admin: paginated Material table with status filter
            │   ├── trip-detail/       #   Signal-polled live trip status + timeline
            │   ├── request-ride/      #   Rider: pickup/dropoff form + fare estimate preview
            │   └── trip-history/      #   Rider trip history (placeholder shell - see TODO)
            ├── drivers/
            │   ├── driver-list/       #   Admin: paginated Material table of drivers
            │   └── incoming-request/  #   Driver: incoming trip request page (placeholder - see TODO)
            └── shared/
                ├── layout/            #   MainLayoutComponent - mat-sidenav + mat-toolbar shell
                └── components/        #   StatCardComponent, TripStatusChipComponent,
                                          LegacyTripCardComponent (unused/dead code)
```

**Standalone components only** — no `NgModule`s anywhere in `app/`. Routes
use `loadComponent()` for lazy loading per feature.
