package com.tourplanner.repository;

import com.tourplanner.model.entity.TourEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourRepository extends JpaRepository<TourEntity, Long> {

    // Ownership-safe lookup
    Optional<TourEntity> findByIdAndUserId(Long id, Long userId);

    // All tours for a given user
    List<TourEntity> findAllByUserId(Long userId);
}
