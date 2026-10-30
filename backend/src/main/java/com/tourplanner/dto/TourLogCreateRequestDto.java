package com.tourplanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TourLogCreateRequestDto(
    @NotNull Long tourId,
    @NotBlank String loggedAt,
    @NotBlank String comment,
    @NotNull Integer difficulty,
    @NotNull Double totalDistanceKm,
    @NotNull Integer totalTimeMin,
    @NotNull Integer rating
) {
}
