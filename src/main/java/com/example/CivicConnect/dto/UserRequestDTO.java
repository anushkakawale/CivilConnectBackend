package com.example.CivicConnect.dto;

import com.example.CivicConnect.entity.enums.RoleName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequestDTO {
	
	@NotBlank(message = "Name is required")
	private String name;
	
	@Email(message = "Invalid Email Format")
	@NotBlank(message = "Email is required")
	private String email;
	
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
	private String mobile;
	
    @NotBlank(message = "Password is required")
	private String password;
	
    @NotNull(message = "Role is required")
	private RoleName role;
}
