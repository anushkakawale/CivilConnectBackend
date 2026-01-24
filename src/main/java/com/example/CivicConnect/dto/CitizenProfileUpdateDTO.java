package com.example.CivicConnect.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CitizenProfileUpdateDTO {

    // ✅ Name update allowed
    private String name;

    // ❗ Ward change → request only
    private Long wardId;

    // 📱 Mobile handled ONLY via OTP flow (not here)
    @Pattern(
        regexp = "^[6-9][0-9]{9}$",
        message = "Invalid mobile number"
    )
    private String mobile;
}
