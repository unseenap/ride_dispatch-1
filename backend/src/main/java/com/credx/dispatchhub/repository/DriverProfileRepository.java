package com.credx.dispatchhub.repository;

import com.credx.dispatchhub.entity.DriverProfile;
import com.credx.dispatchhub.enums.DriverStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

    Optional<DriverProfile> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DriverProfile d where d.user.id = :userId")
    Optional<DriverProfile> findByUserIdForUpdate(@Param("userId") Long userId);

    @EntityGraph(attributePaths = "user")
    Page<DriverProfile> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<DriverProfile> findByStatus(DriverStatus status, Pageable pageable);

    List<DriverProfile> findByStatus(DriverStatus status);

    @Query("select d from DriverProfile d join fetch d.user where d.id = :id")
    Optional<DriverProfile> findByIdWithUser(@Param("id") Long id);

    // TODO: implement a real "nearby drivers" query, e.g. using a bounding-box
    // pre-filter on currentLat/currentLng followed by a haversine distance
    // check, or PostGIS ST_DWithin if we add the extension. For now this is
    // unused - see DriverService#findNearbyAvailableDrivers.
    @Query("select d from DriverProfile d where d.status = com.credx.dispatchhub.enums.DriverStatus.AVAILABLE")
    List<DriverProfile> findAllAvailableForNearbySearch();
}
