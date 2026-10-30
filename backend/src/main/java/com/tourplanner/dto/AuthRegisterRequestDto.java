package com.tourplanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuthRegisterRequestDto(
	@NotBlank String username,
	@NotBlank
	@Pattern(
		regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
		message = "Password must be at least 8 characters long, contain at least one uppercase letter and one number."
	)
	String password
) {
}
