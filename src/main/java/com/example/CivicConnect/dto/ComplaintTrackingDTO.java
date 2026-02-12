package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.Priority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintTrackingDTO {

    private Long complaintId;
    private String title;
    private String description;
    private ComplaintStatus currentStatus;
    private Priority priority;
    private String category;

    // Location Details
    private String location;
    private String address;
    private Double latitude;
    private Double longitude;
    
    // Ward Information
    private Long wardId;
    private String wardNumber;
    private String wardName;
    
    // Department Information
    private Long departmentId;
    private String departmentName;

    // Dates
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    
    // Officer details
    private String officerName;
    private String assignedOfficerMobile;

    // SLA Information
    private LocalDateTime slaDeadline;
    private boolean slaBreached;

    private List<StatusHistoryDTO> history;

    // Additional Details
    private List<String> images;
    private Integer rating;
    private String feedback;
    private List<AuditLogDTO> auditLogs; // Added for detailed tracking
}
