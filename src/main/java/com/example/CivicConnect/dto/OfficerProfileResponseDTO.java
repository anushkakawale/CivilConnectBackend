package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficerProfileResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private String department;
    private String ward;
    private Integer activeComplaintCount;
    private Boolean active;
    private String profileStatus;
}
