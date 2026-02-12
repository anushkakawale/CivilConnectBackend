package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import lombok.*;

/**
 * DTO for tracking complaints pending closure by Admin
 * Shows APPROVED complaints waiting for final admin closure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingClosureTrackingDTO {
    
    // Basic Info
    private Long id;
    private String title;
    private String description;
    
    // Department & Location
    private String departmentName;
    private String wardName;
    private String priority;
    
    // Actors
    private String citizenName;
    private String citizenMobile;
    private String assignedOfficerName;
    private String assignedOfficerMobile;
    private String approvedByName;
    
    // Status & SLA
    private String status;
    private String slaStatus;
    private Boolean slaBreached;
    private LocalDateTime slaDeadline;
    
    // Timeline
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime approvedAt;
    
    // Days waiting for closure
    private Long daysWaitingForClosure;
    
    // Remarks
    private String resolutionRemarks;
    private String approvalRemarks;
    
    // Image verification
    private Integer beforeImageCount;
    private Integer afterImageCount;
    private Boolean hasResolutionImages;
}
