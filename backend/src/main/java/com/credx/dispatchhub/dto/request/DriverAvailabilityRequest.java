package com.credx.dispatchhub.dto.request;

import com.credx.dispatchhub.enums.DriverStatus;
import jakarta.validation.constraints.NotNull;

public record DriverAvailabilityRequest(
        @NotNull(message = "Status is required")
        DriverStatus status
) {
}
