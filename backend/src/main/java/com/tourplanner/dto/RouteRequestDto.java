package com.tourplanner.dto;

import com.tourplanner.model.entity.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RouteRequestDto(
    @NotBlank String from,
    @NotBlank String to,
    @NotNull TransportType transportType
) {
}
