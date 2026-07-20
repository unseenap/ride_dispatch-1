package com.credx.dispatchhub.dto.response;

import com.credx.dispatchhub.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripStatusHistoryResponse {
    private TripStatus status;
    private Instant changedAt;
    private String note;
}
