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
public class DepartmentOfficerRegistrationDTO {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @Size(min = 10, max = 10)
    @NotBlank
    private String mobile;

    @NotBlank
    private String password;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Ward is required")
    private Long wardId;
}
