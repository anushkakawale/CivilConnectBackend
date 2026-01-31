package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponseDTO {

    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private String role;

    // citizen-only fields
    private Long wardId;
    private String wardNumber;
    private String areaName;

    // officer fields
    private Long departmentId;
    private String departmentName;
    private String designation;
    private String employeeId;
}
