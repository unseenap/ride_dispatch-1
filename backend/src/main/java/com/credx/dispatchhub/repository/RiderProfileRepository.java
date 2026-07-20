package com.credx.dispatchhub.repository;

import com.credx.dispatchhub.entity.RiderProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiderProfileRepository extends JpaRepository<RiderProfile, Long> {

    Optional<RiderProfile> findByUserId(Long userId);
}
