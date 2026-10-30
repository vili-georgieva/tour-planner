package com.tourplanner.dto;

import jakarta.validation.constraints.NotBlank;

public record SearchRequestDto(@NotBlank String query) {
}
