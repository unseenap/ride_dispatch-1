package com.credx.dispatchhub.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DriverProfileUpdateRequest(
        @NotBlank(message = "Vehicle make is required")
        String vehicleMake,

        @NotBlank(message = "Vehicle model is required")
        String vehicleModel,

        String vehicleColor,

        @NotBlank(message = "License plate is required")
        String licensePlate
) {
}
