package com.credx.dispatchhub.dto.response;

import com.credx.dispatchhub.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private Long id;

    private Long riderId;
    private String riderName;

    private Long driverId;
    private String driverName;
    private String driverVehicle;
    private String driverLicensePlate;

    private Double pickupLat;
    private Double pickupLng;
    private String pickupAddress;

    private Double dropoffLat;
    private Double dropoffLng;
    private String dropoffAddress;

    private TripStatus status;

    private Instant requestedAt;
    private Instant acceptedAt;
    private Instant arrivedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private String cancellationReason;

    private BigDecimal fareEstimate;
    private BigDecimal finalFare;
    private Double distanceKm;

    private List<TripStatusHistoryResponse> statusHistory;
}
