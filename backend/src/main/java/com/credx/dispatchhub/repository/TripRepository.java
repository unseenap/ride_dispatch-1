package com.credx.dispatchhub.repository;

import com.credx.dispatchhub.entity.Trip;
import com.credx.dispatchhub.enums.TripStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @EntityGraph(attributePaths = {"rider", "driver", "driver.user"})
    Page<Trip> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"rider", "driver", "driver.user"})
    Page<Trip> findByStatus(TripStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"rider", "driver", "driver.user"})
    Page<Trip> findByRiderId(Long riderId, Pageable pageable);

    @EntityGraph(attributePaths = {"rider", "driver", "driver.user"})
    Page<Trip> findByRiderIdAndStatus(Long riderId, TripStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"rider", "driver", "driver.user"})
    Page<Trip> findByDriverId(Long driverId, Pageable pageable);

    @EntityGraph(attributePaths = {"rider", "driver", "driver.user"})
    List<Trip> findByRiderIdOrderByRequestedAtDesc(Long riderId);

    @Query("select t from Trip t left join fetch t.rider left join fetch t.driver d left join fetch d.user where t.id = :id")
    Optional<Trip> findByIdWithRiderAndDriver(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t where t.id = :id")
    Optional<Trip> findByIdForUpdate(@Param("id") Long id);

    long countByStatusAndRequestedAtBetween(TripStatus status, Instant from, Instant to);

    long countByRequestedAtBetween(Instant from, Instant to);

    List<Trip> findByStatusIn(List<TripStatus> statuses);

    interface DriverTripAggregate {
        Long getDriverId();
        String getDriverName();
        long getCompletedTrips();
        BigDecimal getTotalRevenue();
    }

    @Query("""
            select t.driver.id as driverId,
                   t.driver.user.fullName as driverName,
                   count(t) as completedTrips,
                   sum(coalesce(t.finalFare, t.fareEstimate)) as totalRevenue
            from Trip t
            where t.status = com.credx.dispatchhub.enums.TripStatus.COMPLETED
              and t.driver is not null
              and t.requestedAt between :from and :to
            group by t.driver.id, t.driver.user.fullName
            """)
    List<DriverTripAggregate> aggregateCompletedTripsPerDriver(@Param("from") Instant from, @Param("to") Instant to);

    interface DriverEarningsAggregate {
        long getCompletedTrips();
        BigDecimal getTotalEarnings();
        Double getTotalDistanceKm();
    }

    @Query("""
            select count(t) as completedTrips,
                   coalesce(sum(coalesce(t.finalFare, t.fareEstimate)), 0) as totalEarnings,
                   coalesce(sum(t.distanceKm), 0) as totalDistanceKm
            from Trip t
            where t.status = com.credx.dispatchhub.enums.TripStatus.COMPLETED
              and t.driver.id = :driverId
              and t.completedAt between :from and :to
            """)
    DriverEarningsAggregate aggregateEarningsForDriver(@Param("driverId") Long driverId,
                                                       @Param("from") Instant from,
                                                       @Param("to") Instant to);
}
