package com.example.CivicConnect.dto.citizen;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
