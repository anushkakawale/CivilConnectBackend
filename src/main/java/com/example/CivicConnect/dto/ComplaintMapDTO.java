package com.example.CivicConnect.dto;

import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.SLAStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintMapDTO {
    private Long complaintId;
    private Double latitude;
    private Double longitude;
    private ComplaintStatus status;
    private SLAStatus slaStatus;
    private String title;
    private String description;
    private String imageUrl;
    private String departmentName;
    private String wardName;
    private String priority;
    private Boolean slaBreached;
    private java.time.LocalDateTime createdAt;
    
    // Compatibility Constructor for MapService (if old calls exist)
    public ComplaintMapDTO(Long complaintId, Double latitude, Double longitude, ComplaintStatus status, SLAStatus slaStatus) {
        this.complaintId = complaintId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.slaStatus = slaStatus;
    }
}