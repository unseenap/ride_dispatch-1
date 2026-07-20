package com.credx.dispatchhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverEarningsResponse {
    private Long driverId;
    private Instant from;
    private Instant to;
    private long completedTrips;
    private BigDecimal totalEarnings;
    private BigDecimal averageFare;
    private double totalDistanceKm;
}
