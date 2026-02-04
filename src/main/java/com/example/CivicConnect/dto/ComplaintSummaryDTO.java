package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintSummaryDTO {

    private Long complaintId;
    private String title;
    private ComplaintStatus status;
    private com.example.CivicConnect.entity.enums.Priority priority;
    private String departmentName;
    private String wardName;
    private String imageUrl;
    private LocalDateTime createdAt;
    
    // Compatibility Constructor for GlobalSearchService
    public ComplaintSummaryDTO(Long complaintId, String title, ComplaintStatus status, LocalDateTime createdAt) {
        this.complaintId = complaintId;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
    }
}
