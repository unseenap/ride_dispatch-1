package com.credx.dispatchhub.repository;

import com.credx.dispatchhub.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByTripId(Long tripId);

    List<Review> findByDriverId(Long driverId);

    @Query("select avg(r.rating) from Review r where r.driver.id = :driverId")
    Double averageRatingForDriver(@Param("driverId") Long driverId);
}
