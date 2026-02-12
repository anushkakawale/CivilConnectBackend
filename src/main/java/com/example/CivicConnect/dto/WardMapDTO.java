package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardMapDTO {
    private Long wardId;
    private String wardNumber;
    private String areaName;
    private String boundaryCoords; // GeoJSON string
    
    // Stats for map overlay
    private Long totalComplaints;
    private Double resolvedPercentage;
    private Long pendingComplaints;
    private Long slaBreachedCount;
}
