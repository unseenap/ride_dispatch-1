package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.request.ReviewRequest;
import com.credx.dispatchhub.dto.response.ReviewResponse;
import com.credx.dispatchhub.repository.ReviewRepository;
import com.credx.dispatchhub.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Handles rider -> driver ratings submitted after a trip completes.
 *
 * TODO: this service is a stub. Still needed:
 *   - validate the trip belongs to the requesting rider and is COMPLETED
 *   - validate a review doesn't already exist for this trip (Review.trip is
 *     unique, but that should be a friendly 409, not a raw DB constraint error)
 *   - persist the Review entity and recompute DriverProfile.rating (e.g. a
 *     running average) and RiderProfile / DriverProfile totalTrips bookkeeping
 *   - map the saved entity to ReviewResponse
 * No controller currently calls this - see TripController.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TripRepository tripRepository;

    public ReviewResponse submitReview(Long tripId, Long riderId, ReviewRequest request) {
        // TODO: implement rating submission (see class-level TODO above).
        throw new UnsupportedOperationException("Driver rating submission is not implemented yet");
    }
}
