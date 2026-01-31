package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    private long totalComplaints;
    private long pending;
    private long assigned;
    private long inProgress;
    private long resolved;
    private long approved;
    private long closed;
    private long slaBreached;
    private double slaCompliancePercent;
    
    // Ward Officer specific
    private String wardName;
    private Integer wardNumber;
    private long pendingApprovals;
    
    // Department Officer specific
    private String departmentName;
    private long assignedToMe;
}
