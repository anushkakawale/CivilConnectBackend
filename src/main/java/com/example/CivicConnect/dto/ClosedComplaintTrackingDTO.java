package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import lombok.*;

/**
 * DTO for tracking CLOSED complaints (Similar to ApprovalQueueDTO)
 * Used by Ward Officers to see their completed work history
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosedComplaintTrackingDTO {
    
    // Basic Info
    private Long id;
    private String title;
    private String description;
    
    // Department & Location
    private String departmentName;
    private String wardName;
    private String priority;
    
    // Actors in the lifecycle
    private String citizenName;
    private String assignedOfficerName;
    private String approvedByName;
    private String closedByAdminName;
    
    // Status & SLA
    private String status;
    private String slaStatus;
    private Boolean slaBreached;
    
    // Timeline
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime closedAt;
    
    // Remarks & Feedback
    private String approvalRemarks;
    private String closureRemarks;
    
    // Ratings
    private Double averageRating;
    private Integer totalRatings;
    
    // Image count for quick reference
    private Integer beforeImageCount;
    private Integer afterImageCount;
}
