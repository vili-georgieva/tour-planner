package com.tourplanner.dto;

import java.util.List;

public record SearchResponseDto(List<TourResponseDto> tours) {
}
