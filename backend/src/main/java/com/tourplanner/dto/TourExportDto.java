package com.tourplanner.dto;

import com.tourplanner.model.entity.TransportType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TourExportDto(
    @NotBlank String name,
    @NotBlank String description,
    @NotBlank String from,
    @NotBlank String to,
    @NotNull TransportType transportType,
    @NotNull Double distanceKm,
    @NotNull Integer estimatedTimeMin,
    String routeGeometry,
    String imageFilePath,
    @NotNull @Valid List<TourLogExportDto> logs
) {
}
