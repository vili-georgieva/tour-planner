package com.tourplanner.dto;

import com.tourplanner.model.entity.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TourCreateRequestDto(
    @NotBlank String name,
    @NotBlank String description,
    @NotBlank String from,
    @NotBlank String to,
    @NotNull TransportType transportType,
    String imageFilePath
) {
}
