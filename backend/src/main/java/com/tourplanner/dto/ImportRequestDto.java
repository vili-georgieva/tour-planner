package com.tourplanner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ImportRequestDto(

    @NotEmpty @Valid List<TourExportDto> tours
) {
}
