package com.example.CivicConnect.dto.citizen;

import lombok.Data;

@Data
public class CitizenDashboardSummaryDTO {
	private String citizenName;
    private String wardName;
    private Long totalComplaints;
    private Long inProgress;
    private Long resolved;
    private Long pendingApproval;
}
