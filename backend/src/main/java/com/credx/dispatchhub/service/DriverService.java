package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.request.DriverAvailabilityRequest;
import com.credx.dispatchhub.dto.request.DriverLocationUpdateRequest;
import com.credx.dispatchhub.dto.request.DriverProfileUpdateRequest;
import com.credx.dispatchhub.dto.response.DriverProfileResponse;
import com.credx.dispatchhub.entity.DriverProfile;
import com.credx.dispatchhub.enums.DriverStatus;
import com.credx.dispatchhub.exception.ResourceNotFoundException;
import com.credx.dispatchhub.repository.DriverProfileRepository;
import com.credx.dispatchhub.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverService {

    private static final double KM_PER_DEGREE_LAT = 111.0;
    private static final double MAX_SEARCH_RADIUS_KM = 50.0;
    private static final int MAX_NEARBY_RESULTS = 20;

    private final DriverProfileRepository driverProfileRepository;

    @Transactional(readOnly = true)
    public Page<DriverProfileResponse> listDrivers(DriverStatus status, Pageable pageable) {
        Page<DriverProfile> page = (status != null)
                ? driverProfileRepository.findByStatus(status, pageable)
                : driverProfileRepository.findAll(pageable);

        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DriverProfileResponse getDriverById(Long id) {
        DriverProfile driver = driverProfileRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        return toResponse(driver);
    }

    @Transactional(readOnly = true)
    public DriverProfileResponse getDriverByUserId(Long userId) {
        DriverProfile driver = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for this user"));
        return toResponse(driver);
    }

    @Transactional
    public DriverProfileResponse updateAvailability(Long userId, DriverAvailabilityRequest request) {
        DriverProfile driver = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for this user"));

        if (driver.getStatus() == DriverStatus.ON_TRIP && request.status() != DriverStatus.ON_TRIP) {
            throw new IllegalArgumentException("Cannot change availability while a trip is in progress");
        }

        if (driver.getStatus() != DriverStatus.ON_TRIP && request.status() == DriverStatus.ON_TRIP) {
            throw new IllegalArgumentException("ON_TRIP status is managed when accepting a trip");
        }

        driver.setStatus(request.status());
        return toResponse(driverProfileRepository.save(driver));
    }

    @Transactional
    public DriverProfileResponse updateLocation(Long userId, DriverLocationUpdateRequest request) {
        DriverProfile driver = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for this user"));

        driver.setCurrentLat(request.lat());
        driver.setCurrentLng(request.lng());
        return toResponse(driverProfileRepository.save(driver));
    }

    @Transactional
    public DriverProfileResponse updateProfile(Long userId, DriverProfileUpdateRequest request) {
        DriverProfile driver = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for this user"));

        driver.setVehicleMake(request.vehicleMake());
        driver.setVehicleModel(request.vehicleModel());
        driver.setVehicleColor(request.vehicleColor());
        driver.setLicensePlate(request.licensePlate());
        return toResponse(driverProfileRepository.save(driver));
    }

    /**
     * Nearby available drivers: bounding-box pre-filter in the database, then
     * a precise haversine distance check, sorted nearest-first and capped.
     */
    @Transactional(readOnly = true)
    public List<DriverProfileResponse> findNearbyAvailableDrivers(double lat, double lng, double radiusKm) {
        if (radiusKm <= 0 || radiusKm > MAX_SEARCH_RADIUS_KM) {
            throw new IllegalArgumentException("radiusKm must be between 0 and " + MAX_SEARCH_RADIUS_KM);
        }

        // 1 degree of latitude is ~111 km; longitude degrees shrink by cos(lat).
        double latDelta = radiusKm / KM_PER_DEGREE_LAT;
        double lngDelta = radiusKm / (KM_PER_DEGREE_LAT * Math.max(Math.cos(Math.toRadians(lat)), 0.01));

        return driverProfileRepository.findAvailableWithinBoundingBox(
                        lat - latDelta, lat + latDelta, lng - lngDelta, lng + lngDelta)
                .stream()
                .map(d -> Map.entry(d, GeoUtils.distanceKm(lat, lng, d.getCurrentLat(), d.getCurrentLng())))
                .filter(e -> e.getValue() <= radiusKm)
                .sorted(Map.Entry.comparingByValue())
                .limit(MAX_NEARBY_RESULTS)
                .map(e -> toResponse(e.getKey()))
                .toList();
    }

    private DriverProfileResponse toResponse(DriverProfile driver) {
        return DriverProfileResponse.builder()
                .id(driver.getId())
                .userId(driver.getUser().getId())
                .fullName(driver.getUser().getFullName())
                .email(driver.getUser().getEmail())
                .phoneNumber(driver.getUser().getPhoneNumber())
                .vehicleMake(driver.getVehicleMake())
                .vehicleModel(driver.getVehicleModel())
                .vehicleColor(driver.getVehicleColor())
                .licensePlate(driver.getLicensePlate())
                .status(driver.getStatus())
                .currentLat(driver.getCurrentLat())
                .currentLng(driver.getCurrentLng())
                .rating(driver.getRating())
                .totalTrips(driver.getTotalTrips())
                .build();
    }
}
