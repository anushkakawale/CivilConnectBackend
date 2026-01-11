package com.example.CivicConnect.dto.citizen;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CitizenProfileCompletionDTO {
	@NotBlank(message="Area name is required")
	private String areaName;
}
