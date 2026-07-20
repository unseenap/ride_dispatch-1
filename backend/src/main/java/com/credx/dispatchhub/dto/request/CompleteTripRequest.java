package com.credx.dispatchhub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

/**
 * Optional override for the final fare when completing a trip (e.g. tolls,
 * a manual adjustment by the driver app). When omitted, the service falls
 * back to the fare estimate computed at request time.
 */
public record CompleteTripRequest(
        @DecimalMin(value = "0.00", message = "Final fare must not be negative")
        @Digits(integer = 8, fraction = 2, message = "Final fare must have at most 8 integer digits and 2 decimal places")
        BigDecimal finalFare
) {
}
