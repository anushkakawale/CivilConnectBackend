package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintDetailDTO {
    private Long complaintId;
    private String title;
    private String description;
    private String location;
    private String category;
    private ComplaintStatus status;
    private Priority priority;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relation Details
    private String citizenName;
    private String citizenMobile;
    private String assignedOfficerName;
    private String assignedOfficerMobile;
    private String assignedOfficerEmail;
    private String departmentName;
    private Long departmentId;
    private String wardName;
    private Long wardId;
    private String wardNumber;
    
    // SLA Details
    private SlaDetailDTO slaDetails;
    
    // Deprecated simple fields (kept for backward compatibility)
    @Deprecated
    private LocalDateTime slaDeadline;
    @Deprecated
    private boolean slaBreached;
    @Deprecated
    private String slaStatus;
    
    // Status & Feedback
    private LocalDateTime closedAt;
    private Integer rating;
    private String feedback;
    private boolean canReopen;
    
    // Media (with attribution)
    private List<ComplaintImageDTO> images;
    
    // Legacy support (deprecated - use images instead)
    @Deprecated
    private List<String> imageUrls;
    
    // History
    private List<StatusHistoryDTO> history;
    
    // Audit Logs
    private List<AuditLogDTO> auditLogs;

    // Aggregated Ratings
    private Double averageRating;
    private Integer totalRatings;
    private List<FeedbackDisplayDTO> feedbacks;
}
