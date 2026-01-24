package com.example.CivicConnect.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitizenRegistrationDTO {
	@NotBlank(message = "Name is required")
	private String name;
	
	@Email(message = "Invalid Email")
	@NotBlank(message = "Email is required")
	private String email; 
	
	@Size(min = 10, max = 10, message = "Mobile must be 10 digits")
    @NotBlank(message = "Mobile is required")
    private String mobile;
	
    @NotBlank(message = "Password is required")
    private String password;
    
    @Column(nullable = true)
    private String wardNumber;
}
