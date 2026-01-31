package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.SLAStatus;

public class ComplaintDashboardDTO {
    private Long complaintId;
    private String title;
    private ComplaintStatus status;
    private SLAStatus slaStatus;
    private LocalDateTime slaDeadline;
    private long remainingMinutes;
}
