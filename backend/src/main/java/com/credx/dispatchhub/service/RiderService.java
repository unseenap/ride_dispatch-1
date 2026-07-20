package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.response.RiderProfileResponse;
import com.credx.dispatchhub.entity.RiderProfile;
import com.credx.dispatchhub.exception.ResourceNotFoundException;
import com.credx.dispatchhub.repository.RiderProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final RiderProfileRepository riderProfileRepository;

    @Transactional(readOnly = true)
    public RiderProfileResponse getRiderByUserId(Long userId) {
        RiderProfile rider = riderProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider profile not found for this user"));
        return toResponse(rider);
    }

    @Transactional(readOnly = true)
    public RiderProfileResponse getRiderById(Long id) {
        RiderProfile rider = riderProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found with id: " + id));
        return toResponse(rider);
    }

    private RiderProfileResponse toResponse(RiderProfile rider) {
        return RiderProfileResponse.builder()
                .id(rider.getId())
                .userId(rider.getUser().getId())
                .fullName(rider.getUser().getFullName())
                .email(rider.getUser().getEmail())
                .phoneNumber(rider.getUser().getPhoneNumber())
                .paymentMethodLabel(rider.getPaymentMethodLabel())
                .paymentMethodLast4(rider.getPaymentMethodLast4())
                .rating(rider.getRating())
                .totalTrips(rider.getTotalTrips())
                .build();
    }
}
