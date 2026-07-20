package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.response.DashboardStatsResponse;
import com.credx.dispatchhub.dto.response.DriverTripStatsResponse;
import com.credx.dispatchhub.entity.DriverProfile;
import com.credx.dispatchhub.entity.Trip;
import com.credx.dispatchhub.enums.DriverStatus;
import com.credx.dispatchhub.enums.TripStatus;
import com.credx.dispatchhub.repository.DriverProfileRepository;
import com.credx.dispatchhub.repository.RiderProfileRepository;
import com.credx.dispatchhub.repository.TripRepository;
import com.credx.dispatchhub.repository.UserRepository;
import com.credx.dispatchhub.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TripRepository tripRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final RiderProfileRepository riderProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        Instant now = Instant.now();
        Instant startOfDay = DateTimeUtils.startOfDayUtc(now);
        Instant endOfDay = DateTimeUtils.endOfDayUtc(now);

        long totalTripsToday = tripRepository.countByRequestedAtBetween(startOfDay, endOfDay);
        long completedToday = tripRepository.countByStatusAndRequestedAtBetween(TripStatus.COMPLETED, startOfDay, endOfDay);
        long cancelledToday = tripRepository.countByStatusAndRequestedAtBetween(TripStatus.CANCELLED, startOfDay, endOfDay);
        long inProgress = tripRepository.findByStatusIn(List.of(TripStatus.IN_PROGRESS, TripStatus.ARRIVED)).size();
        long activeDrivers = driverProfileRepository.findByStatus(DriverStatus.AVAILABLE).size()
                + driverProfileRepository.findByStatus(DriverStatus.ON_TRIP).size();

        return DashboardStatsResponse.builder()
                .totalTripsToday(totalTripsToday)
                .activeDrivers(activeDrivers)
                .completedTripsToday(completedToday)
                .cancelledTripsToday(cancelledToday)
                .tripsInProgress(inProgress)
                .totalRegisteredDrivers(driverProfileRepository.count())
                .totalRegisteredRiders(riderProfileRepository.count())
                .build();
    }

    /**
     * "Trips per driver" analytics for the given period. Loads every trip row
     * in the window into memory and aggregates in Java rather than pushing the
     * grouping/summing down to the database with a JPQL/native aggregate query
     * - works fine against the seed dataset, but doesn't scale.
     */
    @Transactional(readOnly = true)
    public List<DriverTripStatsResponse> getTripsPerDriver(Instant from, Instant to) {
        List<Trip> trips = tripRepository.findAllForAnalytics(from, to);

        Map<Long, List<Trip>> byDriver = new HashMap<>();
        for (Trip trip : trips) {
            if (trip.getDriver() == null || trip.getStatus() != TripStatus.COMPLETED) {
                continue;
            }
            byDriver.computeIfAbsent(trip.getDriver().getId(), k -> new ArrayList<>()).add(trip);
        }

        List<DriverTripStatsResponse> results = new ArrayList<>();
        for (Map.Entry<Long, List<Trip>> entry : byDriver.entrySet()) {
            List<Trip> driverTrips = entry.getValue();
            DriverProfile driver = driverProfileRepository.findByIdWithUser(entry.getKey()).orElse(null);
            if (driver == null) {
                continue;
            }

            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (Trip trip : driverTrips) {
                BigDecimal fare = trip.getFinalFare() != null ? trip.getFinalFare() : trip.getFareEstimate();
                if (fare != null) {
                    totalRevenue = totalRevenue.add(fare);
                }
            }

            BigDecimal averageFare = driverTrips.isEmpty()
                    ? BigDecimal.ZERO
                    : totalRevenue.divide(BigDecimal.valueOf(driverTrips.size()), 2, RoundingMode.HALF_UP);

            results.add(DriverTripStatsResponse.builder()
                    .driverId(driver.getId())
                    .driverName(driver.getUser().getFullName())
                    .completedTrips(driverTrips.size())
                    .totalRevenue(totalRevenue)
                    .averageFare(averageFare)
                    .build());
        }

        return results;
    }
}
