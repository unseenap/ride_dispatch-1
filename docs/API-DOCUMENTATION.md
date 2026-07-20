# DispatchHub — API Documentation

Base URL (local dev): `http://localhost:8080/api`

All request/response bodies are JSON. All timestamps are ISO-8601 UTC
(`Instant`, e.g. `2026-07-19T14:32:01.123Z`).

## Authentication

Except for `/api/auth/**` and the actuator health endpoints, every endpoint
requires a `Authorization: Bearer <token>` header. Tokens are issued by
`/api/auth/login` and `/api/auth/register` and expire after
`JWT_EXPIRATION_MS` (default 24h, see `application-example.env`).

### Standard error shape

Errors from `GlobalExceptionHandler` all look like this:

```json
{
  "timestamp": "2026-07-19T14:32:01.123Z",
  "status": 404,
  "error": "Not Found",
  "message": "Trip not found with id: 999",
  "path": "/api/trips/999",
  "fieldErrors": null
}
```

Validation errors (400) additionally populate `fieldErrors`:

```json
{
  "timestamp": "2026-07-19T14:32:01.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/auth/register",
  "fieldErrors": [
    { "field": "phoneNumber", "message": "Phone number must be a valid international number" }
  ]
}
```

### Rate limiting

`/api/auth/**` endpoints are rate limited per client IP with a token bucket:
a burst of up to 10 requests, refilling at 10 requests/minute (configurable
via `RATE_LIMIT_CAPACITY`, `RATE_LIMIT_REFILL_PER_MINUTE`, and
`RATE_LIMIT_ENABLED`). Throttled requests receive `429 Too Many Requests`
with the standard error shape.

---

## Auth

### `POST /api/auth/register`
**Auth**: none

Request:
```json
{
  "email": "rider3@dispatchhub.com",
  "password": "Password123!",
  "fullName": "Jordan Kim",
  "phoneNumber": "+14155550199",
  "role": "RIDER"
}
```

Response `201 Created`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInMs": 86400000,
  "refreshToken": "3q2xJ9kP...opaque-url-safe-base64...",
  "refreshExpiresInMs": 2592000000,
  "user": {
    "id": 8,
    "email": "rider3@dispatchhub.com",
    "fullName": "Jordan Kim",
    "phoneNumber": "+14155550199",
    "role": "RIDER"
  }
}
```

Errors: `400` validation failure, `409` email already registered.

### `POST /api/auth/login`
**Auth**: none

Request:
```json
{ "email": "rider1@dispatchhub.com", "password": "Password123!" }
```

Response `200 OK`: same shape as register's `AuthResponse`.

Errors: `401` invalid credentials or disabled account.

### `POST /api/auth/refresh`
**Auth**: none (the refresh token itself is the credential)

Exchanges a valid refresh token for a new access + refresh token pair.
Refresh tokens are opaque, single-use, and rotated on every call: the
presented token is revoked and a replacement is returned. Presenting an
already-used (rotated) token revokes **all** of that user's refresh tokens,
on the assumption the token was stolen.

Request:
```json
{ "refreshToken": "3q2xJ9kP...opaque-url-safe-base64..." }
```

Response `200 OK`: same shape as register's `AuthResponse`.

Errors: `401` unknown, revoked, or expired refresh token, or disabled account.

### `POST /api/auth/logout`
**Auth**: any authenticated user

Revokes every active refresh token belonging to the caller ("log out
everywhere"). The current access token is not invalidated — it simply
expires on its own (`expiresInMs`).

Response `204 No Content`.

---

## Trips

### `POST /api/trips`
**Auth**: `RIDER`

Creates a new trip request for the authenticated rider and computes a fare
estimate server-side.

Request:
```json
{
  "pickupLat": 37.7749,
  "pickupLng": -122.4194,
  "pickupAddress": "1 Market St, San Francisco, CA",
  "dropoffLat": 37.8044,
  "dropoffLng": -122.2711,
  "dropoffAddress": "Jack London Sq, Oakland, CA"
}
```

Response `201 Created`: a `TripResponse` (see shape below) with `status: "REQUESTED"`.

### `POST /api/trips/estimate-fare`
**Auth**: any authenticated user

Request:
```json
{ "pickupLat": 37.7749, "pickupLng": -122.4194, "dropoffLat": 37.8044, "dropoffLng": -122.2711 }
```

Response `200 OK`:
```json
{ "estimatedFare": 18.40, "distanceKm": 13.2, "estimatedDurationMinutes": 26.4, "surgeMultiplier": 1.0 }
```

`surgeMultiplier` is demand-based: it rises linearly from the base `1.0`
toward `MAX_SURGE_MULTIPLIER` (default `2.5`) as waiting `REQUESTED` trips
outnumber `AVAILABLE` drivers, and is already applied to `estimatedFare`.
Trip requests compute their own fare at request time, so the multiplier a
rider was shown can differ slightly from the one applied if demand shifts
between preview and request.

### `GET /api/trips`
**Auth**: `ADMIN`

Paginated list of all trips.

Query params: `page` (0-based), `size`, `sort`, `status` (optional, one of
`REQUESTED|ACCEPTED|ARRIVED|IN_PROGRESS|COMPLETED|CANCELLED`).

Response `200 OK`:
```json
{
  "content": [ { "id": 1, "status": "COMPLETED", "...": "..." } ],
  "page": 0,
  "size": 20,
  "totalElements": 6,
  "totalPages": 1,
  "last": true
}
```

### `GET /api/trips/my`
**Auth**: `RIDER`

Same shape as above, scoped to the authenticated rider's own trips. Supports
the same `status` filter and pagination params.

### `GET /api/trips/driver/{driverProfileId}`
**Auth**: `ADMIN`, `DRIVER`

Paginated trips assigned to a given driver profile.

### `GET /api/trips/{id}`
**Auth**: any authenticated user

Returns a single trip. Used by both the admin trip-detail view and the
rider's own trip-detail view.

Response `200 OK`:
```json
{
  "id": 1,
  "riderId": 2,
  "riderName": "Riya Sharma",
  "driverId": 1,
  "driverName": "Devon Carter",
  "driverVehicle": "Toyota Camry",
  "driverLicensePlate": "7ABC123",
  "pickupLat": 37.7749,
  "pickupLng": -122.4194,
  "pickupAddress": "1 Market St, San Francisco, CA",
  "dropoffLat": 37.8044,
  "dropoffLng": -122.2711,
  "dropoffAddress": "Jack London Sq, Oakland, CA",
  "status": "COMPLETED",
  "requestedAt": "2026-07-17T10:00:00Z",
  "acceptedAt": "2026-07-17T10:01:00Z",
  "arrivedAt": "2026-07-17T10:05:00Z",
  "startedAt": "2026-07-17T10:06:00Z",
  "completedAt": "2026-07-17T10:25:00Z",
  "cancelledAt": null,
  "cancellationReason": null,
  "fareEstimate": 18.40,
  "finalFare": 19.10,
  "distanceKm": 13.2,
  "statusHistory": [
    { "status": "REQUESTED", "changedAt": "2026-07-17T10:00:00Z", "note": null },
    { "status": "ACCEPTED", "changedAt": "2026-07-17T10:01:00Z", "note": null }
  ]
}
```

Errors: `404` if the trip doesn't exist.

### `POST /api/trips/{id}/accept`
**Auth**: `DRIVER`

Assigns the calling driver to the trip. Requires the driver to currently be
`AVAILABLE` and the trip to be `REQUESTED`.

Response `200 OK`: updated `TripResponse` with `status: "ACCEPTED"`.
Errors: `404` trip not found, `409` trip no longer REQUESTED or driver not AVAILABLE.

### `POST /api/trips/{id}/arrive`
**Auth**: `DRIVER` (must be the assigned driver)

Transitions `ACCEPTED -> ARRIVED`.

### `POST /api/trips/{id}/start`
**Auth**: `DRIVER` (must be the assigned driver)

Transitions `ACCEPTED|ARRIVED -> IN_PROGRESS`.

### `POST /api/trips/{id}/complete`
**Auth**: `DRIVER` (must be the assigned driver)

Transitions `IN_PROGRESS -> COMPLETED`. Body is optional.

Request (optional):
```json
{ "finalFare": 19.10 }
```

If omitted, `finalFare` falls back to the original `fareEstimate`.

### `POST /api/trips/{id}/cancel`
**Auth**: `RIDER`, `DRIVER`, or `ADMIN`

Cancels a trip that is not already `COMPLETED`/`CANCELLED`. Body is optional.

Request (optional):
```json
{ "reason": "Rider changed plans" }
```

Response `200 OK`: updated `TripResponse` with `status: "CANCELLED"`.

### `POST /api/trips/{id}/review`
**Auth**: `RIDER`

Submits a rating/review for a completed trip's driver.

Request:
```json
{ "rating": 5, "comment": "Smooth ride, very punctual driver." }
```

`rating` is required (1–5); `comment` is optional (max 1000 chars).

Response `201 Created`:
```json
{ "id": 1, "tripId": 4, "driverId": 2, "rating": 5, "comment": "Smooth ride, very punctual driver.", "createdAt": "2026-07-20T12:00:00Z" }
```

Errors: `403` if the trip belongs to another rider, `409` if the trip is not
`COMPLETED` or has already been reviewed, `404` if the trip doesn't exist.
Submitting a review recomputes the driver's average rating.

---

## Drivers

### `GET /api/drivers`
**Auth**: `ADMIN`, `DRIVER`

Paginated list of driver profiles. Query params: `page`, `size`, `sort`,
`status` (optional, `OFFLINE|AVAILABLE|ON_TRIP`).

Response `200 OK`:
```json
{
  "content": [
    {
      "id": 1,
      "userId": 4,
      "fullName": "Devon Carter",
      "email": "driver1@dispatchhub.com",
      "phoneNumber": "+14155550201",
      "vehicleMake": "Toyota",
      "vehicleModel": "Camry",
      "vehicleColor": "Silver",
      "licensePlate": "7ABC123",
      "status": "AVAILABLE",
      "currentLat": 37.7749,
      "currentLng": -122.4194,
      "rating": 4.90,
      "totalTrips": 145
    }
  ],
  "page": 0, "size": 20, "totalElements": 4, "totalPages": 1, "last": true
}
```

### `GET /api/drivers/{id}`
**Auth**: `ADMIN`, `DRIVER`

Single driver profile by id. `404` if not found.

### `GET /api/drivers/me`
**Auth**: `DRIVER`

The authenticated driver's own profile.

### `PATCH /api/drivers/me/availability`
**Auth**: `DRIVER`

Go online/offline.

Request:
```json
{ "status": "AVAILABLE" }
```

Errors: `400` if attempting to go `AVAILABLE` while `ON_TRIP`.

### `PATCH /api/drivers/me/location`
**Auth**: `DRIVER`

Request:
```json
{ "lat": 37.7752, "lng": -122.4189 }
```

### `PUT /api/drivers/me/profile`
**Auth**: `DRIVER`

Request:
```json
{ "vehicleMake": "Toyota", "vehicleModel": "Camry", "vehicleColor": "Silver", "licensePlate": "7ABC123" }
```

### `GET /api/drivers/me/earnings`
**Auth**: `DRIVER`

Earnings summary for the authenticated driver's completed trips. Query
params `from` and `to` are optional ISO-8601 instants; the default window
is the last 30 days. Earnings use `finalFare` when recorded, falling back
to `fareEstimate` (the same rule as admin revenue analytics).

Response `200 OK`:
```json
{
  "driverId": 3,
  "from": "2026-06-20T10:00:00Z",
  "to": "2026-07-20T10:00:00Z",
  "completedTrips": 12,
  "totalEarnings": 214.80,
  "averageFare": 17.90,
  "totalDistanceKm": 96.4
}
```

Errors: `400` if `from` is after `to`, `404` if the caller has no driver profile.

### `GET /api/drivers/nearby`
**Auth**: any authenticated user

Query params: `lat`, `lng`, `radiusKm` (default `5`, max `50`).

Returns `AVAILABLE` drivers with a known location within `radiusKm` of the
given point, sorted nearest-first, capped at 20 results. Response is a JSON
array of driver profile objects (same shape as `GET /api/drivers/{id}`).
An out-of-range `radiusKm` returns `400 Bad Request`.

---

## Riders

### `GET /api/riders/me`
**Auth**: `RIDER`

The authenticated rider's own profile.

```json
{
  "id": 1,
  "userId": 2,
  "fullName": "Riya Sharma",
  "email": "rider1@dispatchhub.com",
  "phoneNumber": "+14155550101",
  "paymentMethodLabel": "Visa",
  "paymentMethodLast4": "4242",
  "rating": 4.90,
  "totalTrips": 12
}
```

### `GET /api/riders/{id}`
**Auth**: `ADMIN`

Fetch any rider profile by id.

---

## Admin

### `GET /api/admin/dashboard-stats`
**Auth**: `ADMIN`

```json
{
  "totalTripsToday": 6,
  "activeDrivers": 3,
  "completedTripsToday": 2,
  "cancelledTripsToday": 1,
  "tripsInProgress": 1,
  "totalRegisteredDrivers": 4,
  "totalRegisteredRiders": 2
}
```

### `GET /api/admin/analytics/trips-per-driver`
**Auth**: `ADMIN`

Query params: `from`, `to` (ISO-8601 datetimes, optional — defaults to the
last 30 days).

```json
[
  {
    "driverId": 1,
    "driverName": "Devon Carter",
    "completedTrips": 2,
    "totalRevenue": 28.00,
    "averageFare": 14.00
  }
]
```

### `POST /api/admin/trips/{id}/force-cancel`
**Auth**: `ADMIN`

Recovery endpoint for trips stuck mid-lifecycle (e.g. a driver's app
crashed). Force-cancels a trip in any non-terminal state, bypassing the
normal caller-ownership rules, and returns the assigned driver (if any)
to `AVAILABLE`.

Request (optional body):
```json
{ "reason": "Driver unreachable for 30 minutes" }
```

Response `200 OK`: updated `TripResponse` with `status: "CANCELLED"`.
Returns `409` if the trip is already `COMPLETED` or `CANCELLED`, `404` if it
doesn't exist.

---

## Status codes used throughout

| Code | Meaning |
|------|---------|
| 200  | Success |
| 201  | Resource created |
| 204  | Success, no body |
| 400  | Validation error / bad request |
| 401  | Missing/invalid/expired token, bad credentials |
| 403  | Authenticated but not authorized for this role/resource |
| 404  | Resource not found |
| 409  | Conflict (duplicate email, invalid state transition, driver unavailable, concurrent modification) |
| 500  | Unexpected server error |
| 501  | Endpoint intentionally not implemented yet (see README Missing Features) |
