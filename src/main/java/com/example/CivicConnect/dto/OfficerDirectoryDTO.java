package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Officer Directory
 * Used to display officer information in maps and directories
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficerDirectoryDTO {
    
    // Core fields (Matching success log)
    private Long userId; 
    private String name;
    private String mobile;
    private String email;
    private String role;
    private String department; 
    private String wardNumber; 
    private LocalDateTime lastLoginAt;
    
    // Status and Geo fields
    private Integer activeComplaintsCount;
    private Double latitude;
    private Double longitude;
    private String specialization;

    // Manual 8-arg constructor for OfficerDirectoryService compatibility
    public OfficerDirectoryDTO(Long userId, String name, String mobile, String email, String role, String department, String wardNumber, LocalDateTime lastLoginAt) {
        this.userId = userId;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.role = role;
        this.department = department;
        this.wardNumber = wardNumber;
        this.lastLoginAt = lastLoginAt;
    }

    // Legacy Aliases / Compatibility Setters
    public void setOfficerId(Long id) { this.userId = id; }
    public void setDepartmentName(String dept) { this.department = dept; }
    public void setWardName(String ward) { this.wardNumber = ward; }
}
