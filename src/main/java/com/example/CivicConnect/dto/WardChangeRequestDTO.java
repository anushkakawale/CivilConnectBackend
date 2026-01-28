package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WardChangeRequestDTO {
    private Long requestId;
    private String citizenName;
    private String citizenEmail;
    private String citizenMobile;
    private String oldWardNumber;
    private String newWardNumber;
    private String status;
    private String requestedAt;
    private String decidedAt;
    private String decidedBy;
    private String remarks;
}
