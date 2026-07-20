-- DispatchHub seed data
-- Run this after applying docs/DATABASE-SCHEMA.sql against a fresh
-- `dispatchhub` database. Safe to re-run against an empty database only
-- (no ON CONFLICT handling - it will error on duplicate emails/plates if
-- run twice).
--
-- All seeded passwords are the string "Password123!" hashed with BCrypt
-- (strength 10). If you register through the API instead, the app will
-- generate its own hash - this file is only for directly loading Postgres.
--
-- Password hash below corresponds to: Password123!
-- $2a$10$DowQvW2P8vXe0X6E2mR9UOe1TnFhNq0h1kM3vFqYV1p8xkS3s2Cby

-- =========================================================================
-- Users: 1 admin, 2 riders, 4 drivers
-- =========================================================================
INSERT INTO users (id, email, password_hash, full_name, phone_number, role, enabled, created_at, updated_at) VALUES
(1, 'admin@dispatchhub.com',   '$2a$10$DowQvW2P8vXe0X6E2mR9UOe1TnFhNq0h1kM3vFqYV1p8xkS3s2Cby', 'Ava Admin',      '+14155550100', 'ADMIN',  TRUE, now() - interval '90 days', now() - interval '90 days'),
(2, 'rider1@dispatchhub.com',  '$2a$10$DowQvW2P8vXe0X6E2mR9UOe1TnFhNq0h1kM3vFqYV1p8xkS3s2Cby', 'Riya Sharma',    '+14155550101', 'RIDER',  TRUE, now() - interval '60 days', now() - interval '60 days'),
(3, 'rider2@dispatchhub.com',  '$2a$10$DowQvW2P8vXe0X6E2mR9UOe1TnFhNq0h1kM3vFqYV1p8xkS3s2Cby', 'Marcus Lee',     '+14155550102', 'RIDER',  TRUE, now() - interval '55 days', now() - interval '55 days'),
(4, 'driver1@dispatchhub.com', '$2a$10$DowQvW2P8vXe0X6E2mR9UOe1TnFhNq0h1kM3vFqYV1p8xkS3s2Cby', 'Devon Carter',   '+14155550201', 'DRIVER', TRUE, now() - interval '80 days', now() - interval '80 days'),
(5, 'driver2@dispatchhub.com', '$2a$10$DowQvW2P8vXe0X6E2mR9UOe1TnFhNq0h1kM3vFqYV1p8xkS3s2Cby', 'Priya Nair',     '+14155550202', 'DRIVER', TRUE, now() - interval '75 days', now() - interval '75 days'),
(6, 'driver3@dispatchhub.com', '$2a$10$DowQvW2P8vXe0X6E2mR9UOe1TnFhNq0h1kM3vFqYV1p8xkS3s2Cby', 'Sam Okafor',     '+14155550203', 'DRIVER', TRUE, now() - interval '70 days', now() - interval '70 days'),
(7, 'driver4@dispatchhub.com', '$2a$10$DowQvW2P8vXe0X6E2mR9UOe1TnFhNq0h1kM3vFqYV1p8xkS3s2Cby', 'Elena Petrova',  '+14155550204', 'DRIVER', TRUE, now() - interval '40 days', now() - interval '40 days');

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));

-- =========================================================================
-- Rider profiles
-- =========================================================================
INSERT INTO rider_profiles (id, user_id, payment_method_label, payment_method_last4, rating, total_trips, created_at, updated_at) VALUES
(1, 2, 'Visa',       '4242', 4.9, 12, now() - interval '60 days', now()),
(2, 3, 'Mastercard', '5511', 4.7, 5,  now() - interval '55 days', now());

SELECT setval(pg_get_serial_sequence('rider_profiles', 'id'), (SELECT MAX(id) FROM rider_profiles));

-- =========================================================================
-- Driver profiles (4 drivers - 2 available, 1 on trip, 1 offline)
-- =========================================================================
INSERT INTO driver_profiles (id, user_id, vehicle_make, vehicle_model, vehicle_color, license_plate, status, current_lat, current_lng, rating, total_trips, version, created_at, updated_at) VALUES
(1, 4, 'Toyota',  'Camry',   'Silver', '7ABC123', 'AVAILABLE', 37.7749, -122.4194, 4.90, 145, 0, now() - interval '80 days', now()),
(2, 5, 'Honda',   'Civic',   'Blue',   '8XYZ456', 'ON_TRIP',   37.7849, -122.4094, 4.80, 98,  0, now() - interval '75 days', now()),
(3, 6, 'Tesla',   'Model 3', 'White',  '9DEF789', 'OFFLINE',   37.7649, -122.4294, 4.95, 210, 0, now() - interval '70 days', now()),
(4, 7, 'Ford',    'Fusion',  'Black',  '3GHI234', 'AVAILABLE', 37.7699, -122.4469, 4.60, 33,  0, now() - interval '40 days', now());

SELECT setval(pg_get_serial_sequence('driver_profiles', 'id'), (SELECT MAX(id) FROM driver_profiles));

-- =========================================================================
-- Trips: one of each interesting lifecycle state
-- =========================================================================

-- 1) COMPLETED trip (rider1 + driver1)
INSERT INTO trips (id, rider_id, driver_id, pickup_lat, pickup_lng, pickup_address, dropoff_lat, dropoff_lng, dropoff_address,
                    status, requested_at, accepted_at, arrived_at, started_at, completed_at, fare_estimate, final_fare, distance_km,
                    created_at, updated_at) VALUES
(1, 2, 1, 37.7749, -122.4194, '1 Market St, San Francisco, CA', 37.8044, -122.2711, 'Jack London Sq, Oakland, CA',
 'COMPLETED', now() - interval '2 days', now() - interval '2 days' + interval '1 minute',
 now() - interval '2 days' + interval '5 minutes', now() - interval '2 days' + interval '6 minutes',
 now() - interval '2 days' + interval '25 minutes', 18.40, 19.10, 13.2,
 now() - interval '2 days', now() - interval '2 days' + interval '25 minutes');

-- 2) IN_PROGRESS trip (rider2 + driver2)
INSERT INTO trips (id, rider_id, driver_id, pickup_lat, pickup_lng, pickup_address, dropoff_lat, dropoff_lng, dropoff_address,
                    status, requested_at, accepted_at, arrived_at, started_at, fare_estimate, distance_km, created_at, updated_at) VALUES
(2, 3, 2, 37.7849, -122.4094, '101 California St, San Francisco, CA', 37.7955, -122.3937, 'Ferry Building, San Francisco, CA',
 'IN_PROGRESS', now() - interval '20 minutes', now() - interval '19 minutes',
 now() - interval '16 minutes', now() - interval '15 minutes', 9.75, 3.1,
 now() - interval '20 minutes', now() - interval '15 minutes');

-- 3) REQUESTED trip, unmatched (rider1)
INSERT INTO trips (id, rider_id, driver_id, pickup_lat, pickup_lng, pickup_address, dropoff_lat, dropoff_lng, dropoff_address,
                    status, requested_at, fare_estimate, distance_km, created_at, updated_at) VALUES
(3, 2, NULL, 37.7699, -122.4469, 'Golden Gate Park, San Francisco, CA', 37.7599, -122.4148, 'Mission District, San Francisco, CA',
 'REQUESTED', now() - interval '3 minutes', 11.20, 4.6, now() - interval '3 minutes', now() - interval '3 minutes');

-- 4) CANCELLED trip (rider2)
INSERT INTO trips (id, rider_id, driver_id, pickup_lat, pickup_lng, pickup_address, dropoff_lat, dropoff_lng, dropoff_address,
                    status, requested_at, cancelled_at, cancellation_reason, fare_estimate, distance_km, created_at, updated_at) VALUES
(4, 3, NULL, 37.7833, -122.4167, 'Union Square, San Francisco, CA', 37.8199, -122.4783, 'Golden Gate Bridge Vista Point',
 'CANCELLED', now() - interval '5 hours', now() - interval '5 hours' + interval '90 seconds', 'Rider changed plans',
 14.60, 6.8, now() - interval '5 hours', now() - interval '5 hours' + interval '90 seconds');

-- 5) ACCEPTED trip (rider1 + driver4) - driver en route to pickup
INSERT INTO trips (id, rider_id, driver_id, pickup_lat, pickup_lng, pickup_address, dropoff_lat, dropoff_lng, dropoff_address,
                    status, requested_at, accepted_at, fare_estimate, distance_km, created_at, updated_at) VALUES
(5, 2, 4, 37.7595, -122.4367, 'Dolores Park, San Francisco, CA', 37.7706, -122.4269, 'Castro District, San Francisco, CA',
 'ACCEPTED', now() - interval '4 minutes', now() - interval '3 minutes', 7.30, 2.4, now() - interval '4 minutes', now() - interval '3 minutes');

-- 6) Older COMPLETED trip for analytics variety (rider2 + driver1)
INSERT INTO trips (id, rider_id, driver_id, pickup_lat, pickup_lng, pickup_address, dropoff_lat, dropoff_lng, dropoff_address,
                    status, requested_at, accepted_at, arrived_at, started_at, completed_at, fare_estimate, final_fare, distance_km,
                    created_at, updated_at) VALUES
(6, 3, 1, 37.7749, -122.4194, 'Embarcadero, San Francisco, CA', 37.7900, -122.4300, 'Nob Hill, San Francisco, CA',
 'COMPLETED', now() - interval '10 days', now() - interval '10 days' + interval '2 minutes',
 now() - interval '10 days' + interval '7 minutes', now() - interval '10 days' + interval '8 minutes',
 now() - interval '10 days' + interval '20 minutes', 8.90, 8.90, 2.6,
 now() - interval '10 days', now() - interval '10 days' + interval '20 minutes');

SELECT setval(pg_get_serial_sequence('trips', 'id'), (SELECT MAX(id) FROM trips));

-- =========================================================================
-- Trip status history (mirrors the lifecycle of each trip above)
-- =========================================================================
INSERT INTO trip_status_history (trip_id, status, changed_at, note) VALUES
(1, 'REQUESTED',   now() - interval '2 days', NULL),
(1, 'ACCEPTED',    now() - interval '2 days' + interval '1 minute', NULL),
(1, 'ARRIVED',     now() - interval '2 days' + interval '5 minutes', NULL),
(1, 'IN_PROGRESS', now() - interval '2 days' + interval '6 minutes', NULL),
(1, 'COMPLETED',   now() - interval '2 days' + interval '25 minutes', NULL),

(2, 'REQUESTED',   now() - interval '20 minutes', NULL),
(2, 'ACCEPTED',    now() - interval '19 minutes', NULL),
(2, 'ARRIVED',     now() - interval '16 minutes', NULL),
(2, 'IN_PROGRESS', now() - interval '15 minutes', NULL),

(3, 'REQUESTED',   now() - interval '3 minutes', NULL),

(4, 'REQUESTED',   now() - interval '5 hours', NULL),
(4, 'CANCELLED',   now() - interval '5 hours' + interval '90 seconds', 'Rider changed plans'),

(5, 'REQUESTED',   now() - interval '4 minutes', NULL),
(5, 'ACCEPTED',    now() - interval '3 minutes', NULL),

(6, 'REQUESTED',   now() - interval '10 days', NULL),
(6, 'ACCEPTED',    now() - interval '10 days' + interval '2 minutes', NULL),
(6, 'ARRIVED',     now() - interval '10 days' + interval '7 minutes', NULL),
(6, 'IN_PROGRESS', now() - interval '10 days' + interval '8 minutes', NULL),
(6, 'COMPLETED',   now() - interval '10 days' + interval '20 minutes', NULL);

-- =========================================================================
-- Reviews (only for fully COMPLETED trips)
-- =========================================================================
INSERT INTO reviews (trip_id, rider_id, driver_id, rating, comment, created_at) VALUES
(1, 2, 1, 5, 'Smooth ride, very punctual driver.', now() - interval '2 days' + interval '30 minutes'),
(6, 3, 1, 4, 'Good trip, car could have been a bit cleaner.', now() - interval '10 days' + interval '30 minutes');
