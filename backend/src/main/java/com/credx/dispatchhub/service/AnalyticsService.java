package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.response.DashboardStatsResponse;
import com.credx.dispatchhub.dto.response.DriverTripStatsResponse;
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
import java.util.List;

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

    @Transactional(readOnly = true)
    public List<DriverTripStatsResponse> getTripsPerDriver(Instant from, Instant to) {
        List<DriverTripStatsResponse> results = new ArrayList<>();
        for (TripRepository.DriverTripAggregate row : tripRepository.aggregateCompletedTripsPerDriver(from, to)) {
            BigDecimal totalRevenue = row.getTotalRevenue() != null ? row.getTotalRevenue() : BigDecimal.ZERO;
            BigDecimal averageFare = row.getCompletedTrips() == 0
                    ? BigDecimal.ZERO
                    : totalRevenue.divide(BigDecimal.valueOf(row.getCompletedTrips()), 2, RoundingMode.HALF_UP);

            results.add(DriverTripStatsResponse.builder()
                    .driverId(row.getDriverId())
                    .driverName(row.getDriverName())
                    .completedTrips(row.getCompletedTrips())
                    .totalRevenue(totalRevenue)
                    .averageFare(averageFare)
                    .build());
        }
        return results;
    }
}
