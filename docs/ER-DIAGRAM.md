# DispatchHub — Entity-Relationship Diagram

This mirrors the JPA entities in `backend/src/main/java/com/credx/dispatchhub/entity`
and the tables created in `docs/DATABASE-SCHEMA.sql`.

```mermaid
erDiagram
    USERS ||--o| DRIVER_PROFILES : "has (if role=DRIVER)"
    USERS ||--o| RIDER_PROFILES : "has (if role=RIDER)"
    USERS ||--o{ TRIPS : "requests (as rider)"
    DRIVER_PROFILES ||--o{ TRIPS : "is assigned to"
    TRIPS ||--o{ TRIP_STATUS_HISTORY : "has audit trail"
    TRIPS ||--o| REVIEWS : "may have"
    USERS ||--o{ REVIEWS : "writes (as rider)"
    DRIVER_PROFILES ||--o{ REVIEWS : "receives"

    USERS {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar full_name
        varchar phone_number
        varchar role "RIDER | DRIVER | ADMIN"
        boolean enabled
        timestamptz created_at
        timestamptz updated_at
    }

    DRIVER_PROFILES {
        bigint id PK
        bigint user_id FK "-> USERS.id, unique"
        varchar vehicle_make
        varchar vehicle_model
        varchar vehicle_color
        varchar license_plate
        varchar status "OFFLINE | AVAILABLE | ON_TRIP"
        double current_lat
        double current_lng
        numeric rating
        int total_trips
        bigint version "optimistic lock"
        timestamptz created_at
        timestamptz updated_at
    }

    RIDER_PROFILES {
        bigint id PK
        bigint user_id FK "-> USERS.id, unique"
        varchar payment_method_label
        varchar payment_method_last4
        numeric rating
        int total_trips
        timestamptz created_at
        timestamptz updated_at
    }

    TRIPS {
        bigint id PK
        bigint rider_id FK "-> USERS.id"
        bigint driver_id FK "-> DRIVER_PROFILES.id, nullable"
        double pickup_lat
        double pickup_lng
        varchar pickup_address
        double dropoff_lat
        double dropoff_lng
        varchar dropoff_address
        varchar status "REQUESTED..CANCELLED"
        timestamptz requested_at
        timestamptz accepted_at
        timestamptz arrived_at
        timestamptz started_at
        timestamptz completed_at
        timestamptz cancelled_at
        varchar cancellation_reason
        numeric fare_estimate
        numeric final_fare
        double distance_km
        timestamptz created_at
        timestamptz updated_at
    }

    TRIP_STATUS_HISTORY {
        bigint id PK
        bigint trip_id FK "-> TRIPS.id"
        varchar status
        timestamptz changed_at
        varchar note
    }

    REVIEWS {
        bigint id PK
        bigint trip_id FK "-> TRIPS.id, unique"
        bigint rider_id FK "-> USERS.id"
        bigint driver_id FK "-> DRIVER_PROFILES.id"
        int rating "1-5"
        varchar comment
        timestamptz created_at
    }
```

## Notes on modeling decisions

- **`Trip.rider` references `User`, not `RiderProfile`.** Any user with role
  `RIDER` can request a trip directly; `RiderProfile` exists to hold
  rider-specific extras (payment stub, aggregate rating) rather than being a
  required join for every trip query.
- **`Trip.driver` references `DriverProfile`, not `User`.** Drivers always
  have exactly one `DriverProfile`, and trip-matching logic (availability,
  vehicle info, current location) all lives on that profile, so it's the more
  natural FK target.
- **`DriverProfile.version`** is a JPA `@Version` column intended for
  optimistic locking on the driver-matching path. It exists in the schema but
  is not currently used to guard the accept-trip flow (see Known Bugs in the
  root README).
- **`TripStatusHistory`** is an explicit audit-trail table (rather than an
  embedded/embeddable list) so history rows can be queried and paginated
  independently of the parent trip if needed later.
- **`Review`** has a unique constraint on `trip_id` — at most one rider
  review per trip.
