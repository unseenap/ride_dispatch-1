package com.credx.dispatchhub.controller;

import com.credx.dispatchhub.dto.response.RiderProfileResponse;
import com.credx.dispatchhub.security.CurrentUser;
import com.credx.dispatchhub.service.RiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/riders")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;
    private final CurrentUser currentUser;

    @GetMapping("/me")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<RiderProfileResponse> getMyRiderProfile() {
        return ResponseEntity.ok(riderService.getRiderByUserId(currentUser.id()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiderProfileResponse> getRider(@PathVariable Long id) {
        return ResponseEntity.ok(riderService.getRiderById(id));
    }
}
