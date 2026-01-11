package com.example.CivicConnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OfficerRegistrationDTO {
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
    
    @NotNull
    private Long wardId;
    
    private Long departmentId;
}
