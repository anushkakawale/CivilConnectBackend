package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WardComplaintDTO {
    private Long complaintId;
    private String title;
    private String citizenName;
    private String department;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
    private boolean escalated;
    private boolean slaBreached;
}
