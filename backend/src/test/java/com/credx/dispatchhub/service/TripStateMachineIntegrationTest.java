package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.request.CancelTripRequest;
import com.credx.dispatchhub.dto.request.CompleteTripRequest;
import com.credx.dispatchhub.dto.request.TripRequest;
import com.credx.dispatchhub.dto.response.DriverProfileResponse;
import com.credx.dispatchhub.dto.response.TripResponse;
import com.credx.dispatchhub.entity.DriverProfile;
import com.credx.dispatchhub.entity.User;
import com.credx.dispatchhub.enums.DriverStatus;
import com.credx.dispatchhub.enums.TripStatus;
import com.credx.dispatchhub.enums.UserRole;
import com.credx.dispatchhub.exception.DriverUnavailableException;
import com.credx.dispatchhub.exception.InvalidTripStateException;
import com.credx.dispatchhub.repository.DriverProfileRepository;
import com.credx.dispatchhub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contract tests for the trip state machine:
 *
 *   REQUESTED -> ACCEPTED -> ARRIVED -> IN_PROGRESS -> COMPLETED
 *        \___________\__________\____________\-> CANCELLED
 *
 * Runs the real services against an in-memory H2 database, so transitions,
 * driver-status side effects, and status history are all verified end to end.
 */
@SpringBootTest
@ActiveProfiles("test")
class TripStateMachineIntegrationTest {

    @Autowired
    private TripService tripService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    private User newRider() {
        return userRepository.save(User.builder()
                .email("rider-" + UUID.randomUUID() + "@test.local")
                .passwordHash("x")
                .fullName("Test Rider")
                .role(UserRole.RIDER)
                .enabled(true)
                .build());
    }

    private User newAvailableDriver() {
        User user = userRepository.save(User.builder()
                .email("driver-" + UUID.randomUUID() + "@test.local")
                .passwordHash("x")
                .fullName("Test Driver")
                .role(UserRole.DRIVER)
                .enabled(true)
                .build());
        driverProfileRepository.save(DriverProfile.builder()
                .user(user)
                .vehicleMake("Toyota")
                .vehicleModel("Camry")
                .licensePlate("TEST-" + UUID.randomUUID().toString().substring(0, 8))
                .status(DriverStatus.AVAILABLE)
                .build());
        return user;
    }

    private TripResponse newRequestedTrip(User rider) {
        return tripService.requestTrip(rider.getId(), new TripRequest(
                37.7749, -122.4194, "1 Market St, San Francisco, CA",
                37.8044, -122.2711, "Jack London Sq, Oakland, CA"));
    }

    private DriverProfile driverProfileOf(User driverUser) {
        return driverProfileRepository.findByUserId(driverUser.getId()).orElseThrow();
    }

    @Test
    void requestedTripStartsInRequestedStateWithFareAndHistory() {
        TripResponse trip = newRequestedTrip(newRider());

        assertThat(trip.getStatus()).isEqualTo(TripStatus.REQUESTED);
        assertThat(trip.getFareEstimate()).isGreaterThan(BigDecimal.ZERO);
        assertThat(trip.getDriverId()).isNull();
        assertThat(trip.getStatusHistory()).hasSize(1);
        assertThat(trip.getStatusHistory().get(0).getStatus()).isEqualTo(TripStatus.REQUESTED);
    }

    @Test
    void happyPathRunsThroughAllStatesAndFreesTheDriver() {
        User rider = newRider();
        User driverUser = newAvailableDriver();
        TripResponse trip = newRequestedTrip(rider);

        trip = tripService.acceptTrip(trip.getId(), driverUser.getId());
        assertThat(trip.getStatus()).isEqualTo(TripStatus.ACCEPTED);
        assertThat(trip.getDriverId()).isEqualTo(driverProfileOf(driverUser).getId());
        assertThat(driverProfileOf(driverUser).getStatus()).isEqualTo(DriverStatus.ON_TRIP);

        trip = tripService.markArrived(trip.getId(), driverUser.getId());
        assertThat(trip.getStatus()).isEqualTo(TripStatus.ARRIVED);

        trip = tripService.startTrip(trip.getId(), driverUser.getId());
        assertThat(trip.getStatus()).isEqualTo(TripStatus.IN_PROGRESS);

        int tripsBefore = driverProfileOf(driverUser).getTotalTrips();
        trip = tripService.completeTrip(trip.getId(), driverUser.getId(),
                new CompleteTripRequest(new BigDecimal("21.50")));

        assertThat(trip.getStatus()).isEqualTo(TripStatus.COMPLETED);
        assertThat(trip.getFinalFare()).isEqualByComparingTo("21.50");
        assertThat(trip.getCompletedAt()).isNotNull();
        assertThat(driverProfileOf(driverUser).getStatus()).isEqualTo(DriverStatus.AVAILABLE);
        assertThat(driverProfileOf(driverUser).getTotalTrips()).isEqualTo(tripsBefore + 1);

        assertThat(trip.getStatusHistory())
                .extracting(h -> h.getStatus())
                .containsExactly(TripStatus.REQUESTED, TripStatus.ACCEPTED, TripStatus.ARRIVED,
                        TripStatus.IN_PROGRESS, TripStatus.COMPLETED);
    }

    @Test
    void completingWithoutAFinalFareFallsBackToTheEstimate() {
        User driverUser = newAvailableDriver();
        TripResponse trip = newRequestedTrip(newRider());
        trip = tripService.acceptTrip(trip.getId(), driverUser.getId());
        trip = tripService.startTrip(trip.getId(), driverUser.getId());

        trip = tripService.completeTrip(trip.getId(), driverUser.getId(), null);

        assertThat(trip.getFinalFare()).isEqualByComparingTo(trip.getFareEstimate());
    }

    @Test
    void tripCanOnlyBeAcceptedFromRequestedState() {
        User firstDriver = newAvailableDriver();
        User secondDriver = newAvailableDriver();
        TripResponse trip = newRequestedTrip(newRider());
        tripService.acceptTrip(trip.getId(), firstDriver.getId());

        assertThatThrownBy(() -> tripService.acceptTrip(trip.getId(), secondDriver.getId()))
                .isInstanceOf(InvalidTripStateException.class);
    }

    @Test
    void unavailableDriverCannotAccept() {
        User driverUser = newAvailableDriver();
        DriverProfile profile = driverProfileOf(driverUser);
        profile.setStatus(DriverStatus.OFFLINE);
        driverProfileRepository.save(profile);

        TripResponse trip = newRequestedTrip(newRider());

        assertThatThrownBy(() -> tripService.acceptTrip(trip.getId(), driverUser.getId()))
                .isInstanceOf(DriverUnavailableException.class);
    }

    @Test
    void arrivalAndCompletionRequireTheirPredecessorStates() {
        User driverUser = newAvailableDriver();
        TripResponse requested = newRequestedTrip(newRider());

        // Not assigned yet: driver-side transitions must be rejected.
        assertThatThrownBy(() -> tripService.markArrived(requested.getId(), driverUser.getId()))
                .isInstanceOf(InvalidTripStateException.class);

        TripResponse trip = tripService.acceptTrip(requested.getId(), driverUser.getId());

        // ACCEPTED (not yet IN_PROGRESS): completing must be rejected.
        Long tripId = trip.getId();
        assertThatThrownBy(() -> tripService.completeTrip(tripId, driverUser.getId(), null))
                .isInstanceOf(InvalidTripStateException.class);
    }

    @Test
    void anotherDriverCannotDriveAnAssignedTrip() {
        User assignedDriver = newAvailableDriver();
        User otherDriver = newAvailableDriver();
        TripResponse trip = newRequestedTrip(newRider());
        tripService.acceptTrip(trip.getId(), assignedDriver.getId());

        assertThatThrownBy(() -> tripService.startTrip(trip.getId(), otherDriver.getId()))
                .isInstanceOf(InvalidTripStateException.class);
    }

    @Test
    void cancellingAnAcceptedTripFreesTheDriver() {
        User rider = newRider();
        User driverUser = newAvailableDriver();
        TripResponse trip = newRequestedTrip(rider);
        tripService.acceptTrip(trip.getId(), driverUser.getId());

        TripResponse cancelled = tripService.cancelTrip(trip.getId(), rider.getId(), UserRole.RIDER,
                new CancelTripRequest("Changed my mind"));

        assertThat(cancelled.getStatus()).isEqualTo(TripStatus.CANCELLED);
        assertThat(cancelled.getCancellationReason()).isEqualTo("Changed my mind");
        assertThat(driverProfileOf(driverUser).getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void terminalStatesCannotBeCancelled() {
        User rider = newRider();
        User driverUser = newAvailableDriver();
        TripResponse trip = newRequestedTrip(rider);
        tripService.acceptTrip(trip.getId(), driverUser.getId());
        tripService.startTrip(trip.getId(), driverUser.getId());
        tripService.completeTrip(trip.getId(), driverUser.getId(), null);

        Long tripId = trip.getId();
        assertThatThrownBy(() -> tripService.cancelTrip(tripId, rider.getId(), UserRole.RIDER, null))
                .isInstanceOf(InvalidTripStateException.class);
        assertThatThrownBy(() -> tripService.forceCancelTrip(tripId, "stuck"))
                .isInstanceOf(InvalidTripStateException.class);
    }

    @Test
    void strangerCannotCancelSomeoneElsesTrip() {
        User rider = newRider();
        User otherRider = newRider();
        TripResponse trip = newRequestedTrip(rider);

        assertThatThrownBy(() -> tripService.cancelTrip(trip.getId(), otherRider.getId(), UserRole.RIDER, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminForceCancelRecoversAStuckTripAndFreesTheDriver() {
        User driverUser = newAvailableDriver();
        TripResponse trip = newRequestedTrip(newRider());
        tripService.acceptTrip(trip.getId(), driverUser.getId());
        tripService.startTrip(trip.getId(), driverUser.getId());

        TripResponse cancelled = tripService.forceCancelTrip(trip.getId(), null);

        assertThat(cancelled.getStatus()).isEqualTo(TripStatus.CANCELLED);
        assertThat(cancelled.getCancellationReason()).isEqualTo("Force-cancelled by admin");
        assertThat(driverProfileOf(driverUser).getStatus()).isEqualTo(DriverStatus.AVAILABLE);
        assertThat(cancelled.getStatusHistory())
                .extracting(h -> h.getStatus())
                .endsWith(TripStatus.CANCELLED);
    }

    @Test
    void nearbySearchOnlyReturnsAvailableDriversWithinRadius() {
        // Placed far from the seed data's San Francisco cluster to isolate the assertion.
        double lat = -33.8688, lng = 151.2093; // Sydney

        User nearUser = newAvailableDriver();
        DriverProfile near = driverProfileOf(nearUser);
        near.setCurrentLat(lat + 0.01);
        near.setCurrentLng(lng);
        driverProfileRepository.save(near);

        User farUser = newAvailableDriver();
        DriverProfile far = driverProfileOf(farUser);
        far.setCurrentLat(lat + 1.0); // ~111 km away
        far.setCurrentLng(lng);
        driverProfileRepository.save(far);

        List<DriverProfileResponse> found = driverService.findNearbyAvailableDrivers(lat, lng, 10);

        assertThat(found).extracting(DriverProfileResponse::getId)
                .contains(near.getId())
                .doesNotContain(far.getId());
    }
}
