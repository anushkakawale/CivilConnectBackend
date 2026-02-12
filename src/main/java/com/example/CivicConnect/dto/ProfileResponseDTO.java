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
    private boolean active;
    private String memberSince;
    private String lastLogin;

    // Ward/Geo info (Commonly needed)
    private Long wardId;
    private String wardNumber;
    private String wardName;
    private String areaName;

    // citizen-only address
    private String address;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String pincode;

    // officer fields
    private Long departmentId;
    private String departmentName;
    private String designation;
    private String employeeId;

    // Aliases for frontend compatibility
    private String ward;
    private String department;

    private Integer completionScore;
}
