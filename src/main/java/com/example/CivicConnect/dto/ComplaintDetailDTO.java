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
    private String departmentName;
    private String wardName;
    
    // SLA Details
    private LocalDateTime slaDeadline;
    private boolean slaBreached;
    private String slaStatus;
    
    // Media
    private List<String> imageUrls;
    
    // History
    private List<StatusHistoryDTO> history;
}
