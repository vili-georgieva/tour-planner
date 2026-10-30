package com.tourplanner.repository;

import com.tourplanner.model.entity.TourLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourLogRepository extends JpaRepository<TourLogEntity, Long> {

    // Ownership-safe lookup
    Optional<TourLogEntity> findByIdAndTourUserId(Long id, Long userId);

    // All logs for a given tour
    List<TourLogEntity> findAllByTourId(Long tourId);

    // Count logs for a given tour (used for popularity)
    int countByTourId(Long tourId);
}
