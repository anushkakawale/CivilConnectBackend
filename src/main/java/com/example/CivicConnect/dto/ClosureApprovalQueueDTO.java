package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import lombok.*;

/**
 * DTO for Admin's Closure Approval Queue
 * Similar to ApprovalQueueDTO but for final closure decisions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosureApprovalQueueDTO {
    
    // Basic Info
    private Long id;
    private String title;
    private String description;
    
    // Location & Department
    private String wardName;
    private String departmentName;
    private String priority;
    
    // Actors
    private String citizenName;
    private String assignedOfficerName;
    private String approvedBy;
    
    // Status & SLA
    private String status;
    private String slaStatus;
    private Boolean slaBreached;
    private LocalDateTime slaDeadline;
    
    // Timeline
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime approvedAt;
    
    // Waiting metrics
    private Long daysWaitingForClosure;
    private Long hoursWaitingForClosure;
    
    // Verification
    private String approvalRemarks;
    private String resolutionRemarks;
    private Integer beforeImageCount;
    private Integer afterImageCount;
    private Boolean hasResolutionImages;
    
    // Performance
    private Double averageRating;
    private Integer totalRatings;
}
