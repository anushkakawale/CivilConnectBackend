package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintClosureDTO {
    private Long id;
    private String title;
    private String wardName;
    private String departmentName;
    private String priority;
    
    // Actors
    private String assignedOfficerName;
    private String approvedByOfficerName;
    private String closedByAdminName;
    
    // Status & SLA
    private String status;
    private String slaStatus;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime closedAt;
    
    // Remarks
    private String approvalRemarks;
    private String closureRemarks;
}
