package com.tourplanner.dto;

public record TourLogResponseDto(
    Long id,
    Long tourId,
    String loggedAt,
    String comment,
    Integer difficulty,
    Double totalDistanceKm,
    Integer totalTimeMin,
    Integer rating
) {
}
