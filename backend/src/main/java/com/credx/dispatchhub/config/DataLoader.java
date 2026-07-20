package com.credx.dispatchhub.config;

import com.credx.dispatchhub.entity.DriverProfile;
import com.credx.dispatchhub.entity.RiderProfile;
import com.credx.dispatchhub.entity.Trip;
import com.credx.dispatchhub.entity.TripStatusHistory;
import com.credx.dispatchhub.entity.User;
import com.credx.dispatchhub.enums.DriverStatus;
import com.credx.dispatchhub.enums.TripStatus;
import com.credx.dispatchhub.enums.UserRole;
import com.credx.dispatchhub.repository.DriverProfileRepository;
import com.credx.dispatchhub.repository.RiderProfileRepository;
import com.credx.dispatchhub.repository.TripRepository;
import com.credx.dispatchhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Seeds a small, realistic dataset on startup when the database is empty.
 * This is a convenience for local dev - see seed-data/seed.sql for the
 * equivalent raw SQL version used in the take-home evaluation setup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final RiderProfileRepository riderProfileRepository;
    private final TripRepository tripRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already has data - skipping DataLoader seed step");
            return;
        }

        log.info("Seeding DispatchHub with local development data...");

        User admin = createUser("admin@dispatchhub.com", "Admin123!", "Ava Admin", "+14155550100", UserRole.ADMIN);

        User riderUser1 = createUser("rider1@dispatchhub.com", "Rider123!", "Riya Sharma", "+14155550101", UserRole.RIDER);
        User riderUser2 = createUser("rider2@dispatchhub.com", "Rider123!", "Marcus Lee", "+14155550102", UserRole.RIDER);
        RiderProfile rider1 = createRiderProfile(riderUser1);
        createRiderProfile(riderUser2);

        User driverUser1 = createUser("driver1@dispatchhub.com", "Driver123!", "Devon Carter", "+14155550201", UserRole.DRIVER);
        User driverUser2 = createUser("driver2@dispatchhub.com", "Driver123!", "Priya Nair", "+14155550202", UserRole.DRIVER);
        User driverUser3 = createUser("driver3@dispatchhub.com", "Driver123!", "Sam Okafor", "+14155550203", UserRole.DRIVER);

        DriverProfile driver1 = createDriverProfile(driverUser1, "Toyota", "Camry", "Silver", "7ABC123",
                DriverStatus.AVAILABLE, 37.7749, -122.4194, "4.90");
        DriverProfile driver2 = createDriverProfile(driverUser2, "Honda", "Civic", "Blue", "8XYZ456",
                DriverStatus.AVAILABLE, 37.7849, -122.4094, "4.80");
        DriverProfile driver3 = createDriverProfile(driverUser3, "Tesla", "Model 3", "White", "9DEF789",
                DriverStatus.OFFLINE, 37.7649, -122.4294, "4.95");

        seedCompletedTrip(riderUser1, driver1);
        seedInProgressTrip(riderUser2, driver2);
        seedRequestedTrip(riderUser1);
        seedCancelledTrip(riderUser2);

        log.info("Seed complete: {} users, {} drivers, {} riders, {} trips",
                userRepository.count(), driverProfileRepository.count(), riderProfileRepository.count(), tripRepository.count());
        log.debug("Admin login: admin@dispatchhub.com / Admin123! (driver3 offline: {})", driver3.getStatus());
        log.debug("Rider1 profile id: {}", rider1.getId());
    }

    private User createUser(String email, String rawPassword, String fullName, String phone, UserRole role) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .phoneNumber(phone)
                .role(role)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    private RiderProfile createRiderProfile(User user) {
        RiderProfile profile = RiderProfile.builder()
                .user(user)
                .paymentMethodLabel("Visa")
                .paymentMethodLast4("4242")
                .rating(BigDecimal.valueOf(4.9))
                .totalTrips(0)
                .build();
        return riderProfileRepository.save(profile);
    }

    private DriverProfile createDriverProfile(User user, String make, String model, String color, String plate,
                                               DriverStatus status, double lat, double lng, String rating) {
        DriverProfile profile = DriverProfile.builder()
                .user(user)
                .vehicleMake(make)
                .vehicleModel(model)
                .vehicleColor(color)
                .licensePlate(plate)
                .status(status)
                .currentLat(lat)
                .currentLng(lng)
                .rating(new BigDecimal(rating))
                .totalTrips(0)
                .build();
        return driverProfileRepository.save(profile);
    }

    private void seedCompletedTrip(User rider, DriverProfile driver) {
        Instant requestedAt = Instant.now().minus(2, ChronoUnit.DAYS);
        Trip trip = Trip.builder()
                .rider(rider)
                .driver(driver)
                .pickupLat(37.7749).pickupLng(-122.4194).pickupAddress("1 Market St, San Francisco, CA")
                .dropoffLat(37.8044).dropoffLng(-122.2711).dropoffAddress("Jack London Sq, Oakland, CA")
                .status(TripStatus.COMPLETED)
                .requestedAt(requestedAt)
                .acceptedAt(requestedAt.plusSeconds(60))
                .arrivedAt(requestedAt.plusSeconds(300))
                .startedAt(requestedAt.plusSeconds(360))
                .completedAt(requestedAt.plusSeconds(1500))
                .fareEstimate(new BigDecimal("18.40"))
                .finalFare(new BigDecimal("19.10"))
                .distanceKm(13.2)
                .build();
        addHistory(trip, TripStatus.REQUESTED, requestedAt);
        addHistory(trip, TripStatus.ACCEPTED, requestedAt.plusSeconds(60));
        addHistory(trip, TripStatus.ARRIVED, requestedAt.plusSeconds(300));
        addHistory(trip, TripStatus.IN_PROGRESS, requestedAt.plusSeconds(360));
        addHistory(trip, TripStatus.COMPLETED, requestedAt.plusSeconds(1500));
        tripRepository.save(trip);
    }

    private void seedInProgressTrip(User rider, DriverProfile driver) {
        driver.setStatus(DriverStatus.ON_TRIP);
        driverProfileRepository.save(driver);

        Instant requestedAt = Instant.now().minus(20, ChronoUnit.MINUTES);
        Trip trip = Trip.builder()
                .rider(rider)
                .driver(driver)
                .pickupLat(37.7849).pickupLng(-122.4094).pickupAddress("101 California St, San Francisco, CA")
                .dropoffLat(37.7955).dropoffLng(-122.3937).dropoffAddress("Ferry Building, San Francisco, CA")
                .status(TripStatus.IN_PROGRESS)
                .requestedAt(requestedAt)
                .acceptedAt(requestedAt.plusSeconds(45))
                .arrivedAt(requestedAt.plusSeconds(240))
                .startedAt(requestedAt.plusSeconds(300))
                .fareEstimate(new BigDecimal("9.75"))
                .distanceKm(3.1)
                .build();
        addHistory(trip, TripStatus.REQUESTED, requestedAt);
        addHistory(trip, TripStatus.ACCEPTED, requestedAt.plusSeconds(45));
        addHistory(trip, TripStatus.ARRIVED, requestedAt.plusSeconds(240));
        addHistory(trip, TripStatus.IN_PROGRESS, requestedAt.plusSeconds(300));
        tripRepository.save(trip);
    }

    private void seedRequestedTrip(User rider) {
        Instant requestedAt = Instant.now().minus(3, ChronoUnit.MINUTES);
        Trip trip = Trip.builder()
                .rider(rider)
                .pickupLat(37.7699).pickupLng(-122.4469).pickupAddress("Golden Gate Park, San Francisco, CA")
                .dropoffLat(37.7599).dropoffLng(-122.4148).dropoffAddress("Mission District, San Francisco, CA")
                .status(TripStatus.REQUESTED)
                .requestedAt(requestedAt)
                .fareEstimate(new BigDecimal("11.20"))
                .distanceKm(4.6)
                .build();
        addHistory(trip, TripStatus.REQUESTED, requestedAt);
        tripRepository.save(trip);
    }

    private void seedCancelledTrip(User rider) {
        Instant requestedAt = Instant.now().minus(5, ChronoUnit.HOURS);
        Trip trip = Trip.builder()
                .rider(rider)
                .pickupLat(37.7833).pickupLng(-122.4167).pickupAddress("Union Square, San Francisco, CA")
                .dropoffLat(37.8199).dropoffLng(-122.4783).dropoffAddress("Golden Gate Bridge Vista Point")
                .status(TripStatus.CANCELLED)
                .requestedAt(requestedAt)
                .cancelledAt(requestedAt.plusSeconds(90))
                .cancellationReason("Rider changed plans")
                .fareEstimate(new BigDecimal("14.60"))
                .distanceKm(6.8)
                .build();
        addHistory(trip, TripStatus.REQUESTED, requestedAt);
        addHistory(trip, TripStatus.CANCELLED, requestedAt.plusSeconds(90));
        tripRepository.save(trip);
    }

    private void addHistory(Trip trip, TripStatus status, Instant at) {
        trip.addStatusHistory(TripStatusHistory.builder()
                .status(status)
                .changedAt(at)
                .build());
    }
}
