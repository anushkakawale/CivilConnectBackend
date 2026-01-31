package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.SLAStatus;

import jakarta.annotation.Priority;

public class CitizenDashboardDTO {

    private Long complaintId;
    private String title;
    private ComplaintStatus status;
    private Priority priority;

    private SLAStatus slaStatus;
    private LocalDateTime slaDeadline;
    private long remainingMinutes;
}
