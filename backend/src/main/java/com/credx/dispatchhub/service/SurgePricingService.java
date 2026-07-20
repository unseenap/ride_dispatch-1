package com.credx.dispatchhub.service;

import com.credx.dispatchhub.config.FareProperties;
import com.credx.dispatchhub.enums.DriverStatus;
import com.credx.dispatchhub.enums.TripStatus;
import com.credx.dispatchhub.repository.DriverProfileRepository;
import com.credx.dispatchhub.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Demand-based surge pricing. The multiplier scales with the ratio of
 * waiting (REQUESTED) trips to AVAILABLE drivers:
 *
 *   ratio <= 1 (enough drivers)  -> base multiplier (no surge)
 *   ratio >= MAX_DEMAND_RATIO    -> max multiplier
 *   in between                   -> linear interpolation, rounded to 0.1
 *
 * With no available drivers but waiting trips, max surge applies. The counts
 * are two indexed COUNT queries, cheap enough to run per estimate.
 */
@Service
@RequiredArgsConstructor
public class SurgePricingService {

    private static final double MAX_DEMAND_RATIO = 3.0;

    private final TripRepository tripRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final FareProperties fareProperties;

    @Transactional(readOnly = true)
    public BigDecimal currentMultiplier() {
        BigDecimal base = fareProperties.getSurgeMultiplier();
        BigDecimal max = fareProperties.getMaxSurgeMultiplier();
        if (max.compareTo(base) <= 0) {
            return base;
        }

        long waitingTrips = tripRepository.countByStatus(TripStatus.REQUESTED);
        long availableDrivers = driverProfileRepository.countByStatus(DriverStatus.AVAILABLE);

        if (waitingTrips == 0) {
            return base;
        }
        if (availableDrivers == 0) {
            return max;
        }

        double ratio = (double) waitingTrips / availableDrivers;
        if (ratio <= 1.0) {
            return base;
        }

        double fraction = Math.min((ratio - 1.0) / (MAX_DEMAND_RATIO - 1.0), 1.0);
        BigDecimal surge = base.add(max.subtract(base).multiply(BigDecimal.valueOf(fraction)));
        return surge.setScale(1, RoundingMode.HALF_UP);
    }
}
