package com.example.CivicConnect.dto.citizen;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CitizenProfileUpdateDTO {
	@NotBlank
	private String name;
	private String newPassword;
	private Long wardId;
}
