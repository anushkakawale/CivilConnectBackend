package com.example.CivicConnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WardOfficerRegistrationDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 10, max = 10, message = "Mobile must be 10 digits")
    @NotBlank
    private String mobile;

    @NotBlank
    private String password;

    @NotNull(message = "Ward is required")
    private Long wardId;
}
