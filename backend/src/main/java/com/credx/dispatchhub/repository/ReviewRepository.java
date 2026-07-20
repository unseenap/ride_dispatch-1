package com.credx.dispatchhub.repository;

import com.credx.dispatchhub.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByTripId(Long tripId);

    List<Review> findByDriverId(Long driverId);
}
