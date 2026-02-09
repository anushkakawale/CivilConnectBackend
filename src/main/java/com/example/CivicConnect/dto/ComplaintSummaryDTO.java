package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.Priority;
import com.example.CivicConnect.entity.enums.SLAStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintSummaryDTO {

    private Long complaintId;
    private String title;
    private ComplaintStatus status;
    private Priority priority;
    private String departmentName;
    private String wardName;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String slaStatus;
    
    // Feedback
    private Integer rating;
    private String feedback;
    
    // Compatibility Constructor for GlobalSearchService
    public ComplaintSummaryDTO(Long complaintId, String title, ComplaintStatus status, LocalDateTime createdAt) {
        this.complaintId = complaintId;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
    }
}
