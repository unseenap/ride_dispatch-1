package com.credx.dispatchhub.dto.response;

import com.credx.dispatchhub.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String vehicleMake;
    private String vehicleModel;
    private String vehicleColor;
    private String licensePlate;
    private DriverStatus status;
    private Double currentLat;
    private Double currentLng;
    private BigDecimal rating;
    private int totalTrips;
}
