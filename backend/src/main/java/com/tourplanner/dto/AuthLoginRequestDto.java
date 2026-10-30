package com.tourplanner.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequestDto(
	@NotBlank String username,
	@NotBlank String password
) {
}
