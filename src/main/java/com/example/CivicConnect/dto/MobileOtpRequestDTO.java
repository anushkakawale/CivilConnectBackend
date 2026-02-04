package com.example.CivicConnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileOtpRequestDTO {
    
    @Pattern(regexp = "^[0-9]{10}$", message = "New mobile number must be 10 digits")
    private String newMobile;
}
