package com.credx.dispatchhub.controller;

import com.credx.dispatchhub.dto.response.GeocodeResult;
import com.credx.dispatchhub.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final GeocodingService geocodingService;

    @GetMapping("/geocode")
    public ResponseEntity<List<GeocodeResult>> geocode(@RequestParam String q) {
        return ResponseEntity.ok(geocodingService.geocode(q));
    }
}
