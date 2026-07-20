package com.credx.dispatchhub.repository;

import com.credx.dispatchhub.entity.TripStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripStatusHistoryRepository extends JpaRepository<TripStatusHistory, Long> {

    List<TripStatusHistory> findByTripIdOrderByChangedAtAsc(Long tripId);
}
