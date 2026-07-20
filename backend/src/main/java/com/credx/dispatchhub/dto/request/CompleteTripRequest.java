package com.credx.dispatchhub.dto.request;

import java.math.BigDecimal;

/**
 * Optional override for the final fare when completing a trip (e.g. tolls,
 * a manual adjustment by the driver app). When omitted, the service falls
 * back to the fare estimate computed at request time.
 */
public record CompleteTripRequest(
        BigDecimal finalFare
) {
}
