package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.request.ReviewRequest;
import com.credx.dispatchhub.dto.response.ReviewResponse;
import com.credx.dispatchhub.entity.DriverProfile;
import com.credx.dispatchhub.entity.Review;
import com.credx.dispatchhub.entity.Trip;
import com.credx.dispatchhub.enums.TripStatus;
import com.credx.dispatchhub.exception.DuplicateResourceException;
import com.credx.dispatchhub.exception.InvalidTripStateException;
import com.credx.dispatchhub.exception.ResourceNotFoundException;
import com.credx.dispatchhub.repository.ReviewRepository;
import com.credx.dispatchhub.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Handles rider -> driver ratings submitted after a trip completes.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TripRepository tripRepository;

    @Transactional
    public ReviewResponse submitReview(Long tripId, Long riderId, ReviewRequest request) {
        Trip trip = tripRepository.findByIdWithRiderAndDriver(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        if (!trip.getRider().getId().equals(riderId)) {
            throw new AccessDeniedException("You can only review your own trips");
        }
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new InvalidTripStateException("Only COMPLETED trips can be reviewed");
        }
        if (trip.getDriver() == null) {
            throw new InvalidTripStateException("Trip has no driver to review");
        }
        if (reviewRepository.findByTripId(tripId).isPresent()) {
            throw new DuplicateResourceException("This trip has already been reviewed");
        }

        Review review = reviewRepository.save(Review.builder()
                .trip(trip)
                .rider(trip.getRider())
                .driver(trip.getDriver())
                .rating(request.rating())
                .comment(request.comment())
                .build());

        recomputeDriverRating(trip.getDriver());

        return toResponse(review);
    }

    private void recomputeDriverRating(DriverProfile driver) {
        Double average = reviewRepository.averageRatingForDriver(driver.getId());
        if (average != null) {
            driver.setRating(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
        }
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .tripId(review.getTrip().getId())
                .driverId(review.getDriver().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
