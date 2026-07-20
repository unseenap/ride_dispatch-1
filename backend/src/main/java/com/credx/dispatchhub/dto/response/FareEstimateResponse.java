package com.credx.dispatchhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateResponse {
    private BigDecimal estimatedFare;
    private double distanceKm;
    private double estimatedDurationMinutes;
    private BigDecimal surgeMultiplier;
}
