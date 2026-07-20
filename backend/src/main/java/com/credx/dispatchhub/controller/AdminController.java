package com.credx.dispatchhub.controller;

import com.credx.dispatchhub.dto.response.DashboardStatsResponse;
import com.credx.dispatchhub.dto.response.DriverTripStatsResponse;
import com.credx.dispatchhub.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }

    @GetMapping("/analytics/trips-per-driver")
    public ResponseEntity<List<DriverTripStatsResponse>> getTripsPerDriver(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Instant effectiveTo = to != null ? to : Instant.now();
        Instant effectiveFrom = from != null ? from : effectiveTo.minus(30, ChronoUnit.DAYS);
        return ResponseEntity.ok(analyticsService.getTripsPerDriver(effectiveFrom, effectiveTo));
    }

    /**
     * TODO: admin "stuck trip" recovery endpoint - not implemented yet.
     * Needed behavior: given a tripId, force-transition it to CANCELLED (or
     * allow reassigning to a different available driver) regardless of the
     * normal state machine rules in TripService, for trips stuck in ACCEPTED/
     * ARRIVED/IN_PROGRESS because a driver's app crashed or lost connectivity.
     * Should probably live in TripService as forceCancelTrip(tripId, adminId)
     * / reassignTrip(tripId, newDriverId) rather than duplicating trip state
     * logic here.
     */
    @PostMapping("/trips/{id}/force-cancel")
    public ResponseEntity<Void> forceCancelTrip(@PathVariable Long id) {
        throw new UnsupportedOperationException("Force-cancel for stuck trips is not implemented yet");
    }
}
