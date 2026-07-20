package com.credx.dispatchhub.service;

import com.credx.dispatchhub.config.FareProperties;
import com.credx.dispatchhub.dto.response.FareEstimateResponse;
import com.credx.dispatchhub.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class FareEstimationService {

    // Assumed average city-driving speed used to translate distance into a
    // rough duration for the per-minute portion of the fare.
    private static final double ASSUMED_AVERAGE_SPEED_KMH = 30.0;

    private final FareProperties fareProperties;
    private final SurgePricingService surgePricingService;

    public FareEstimateResponse estimate(double pickupLat, double pickupLng, double dropoffLat, double dropoffLng) {
        double distanceKm = GeoUtils.distanceKm(pickupLat, pickupLng, dropoffLat, dropoffLng);
        double durationMinutes = (distanceKm / ASSUMED_AVERAGE_SPEED_KMH) * 60.0;

        BigDecimal surgeMultiplier = surgePricingService.currentMultiplier();
        BigDecimal fare = calculateFare(distanceKm, durationMinutes, surgeMultiplier);

        return FareEstimateResponse.builder()
                .estimatedFare(fare)
                .distanceKm(round(distanceKm))
                .estimatedDurationMinutes(round(durationMinutes))
                .surgeMultiplier(surgeMultiplier)
                .build();
    }

    /** Fare at the current demand-based surge multiplier. */
    public BigDecimal calculateFare(double distanceKm, double durationMinutes) {
        return calculateFare(distanceKm, durationMinutes, surgePricingService.currentMultiplier());
    }

    public BigDecimal calculateFare(double distanceKm, double durationMinutes, BigDecimal surgeMultiplier) {
        BigDecimal distanceCost = fareProperties.getPerKmRate().multiply(BigDecimal.valueOf(distanceKm));
        BigDecimal timeCost = fareProperties.getPerMinuteRate().multiply(BigDecimal.valueOf(durationMinutes));

        BigDecimal total = fareProperties.getBaseFare()
                .add(distanceCost)
                .add(timeCost)
                .multiply(surgeMultiplier);

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public double distanceKm(double pickupLat, double pickupLng, double dropoffLat, double dropoffLng) {
        return GeoUtils.distanceKm(pickupLat, pickupLng, dropoffLat, dropoffLng);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
