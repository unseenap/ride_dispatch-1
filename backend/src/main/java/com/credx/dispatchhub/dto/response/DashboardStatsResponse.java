package com.credx.dispatchhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalTripsToday;
    private long activeDrivers;
    private long completedTripsToday;
    private long cancelledTripsToday;
    private long tripsInProgress;
    private long totalRegisteredDrivers;
    private long totalRegisteredRiders;
}
