package com.credx.dispatchhub.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TripRequest(

        @NotNull(message = "Pickup latitude is required")
        @DecimalMin(value = "-90.0", message = "Pickup latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Pickup latitude must be <= 90")
        Double pickupLat,

        @NotNull(message = "Pickup longitude is required")
        @DecimalMin(value = "-180.0", message = "Pickup longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Pickup longitude must be <= 180")
        Double pickupLng,

        @NotBlank(message = "Pickup address is required")
        String pickupAddress,

        @NotNull(message = "Dropoff latitude is required")
        @DecimalMin(value = "-90.0", message = "Dropoff latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Dropoff latitude must be <= 90")
        Double dropoffLat,

        @NotNull(message = "Dropoff longitude is required")
        @DecimalMin(value = "-180.0", message = "Dropoff longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Dropoff longitude must be <= 180")
        Double dropoffLng,

        @NotBlank(message = "Dropoff address is required")
        String dropoffAddress
) {
}
