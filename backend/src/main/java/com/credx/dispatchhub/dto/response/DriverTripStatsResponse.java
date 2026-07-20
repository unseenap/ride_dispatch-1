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
public class DriverTripStatsResponse {
    private Long driverId;
    private String driverName;
    private long completedTrips;
    private BigDecimal totalRevenue;
    private BigDecimal averageFare;
}
