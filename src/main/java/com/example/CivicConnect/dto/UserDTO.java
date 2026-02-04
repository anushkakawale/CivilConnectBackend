package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private boolean active;
    private String wardName;
    private String departmentName;
}
