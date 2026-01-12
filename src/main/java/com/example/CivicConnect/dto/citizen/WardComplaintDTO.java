package com.example.CivicConnect.dto.citizen;

import lombok.Data;

@Data
public class WardComplaintDTO {
    private Long complaintId;
    private String title;
    private String departmentName;
    private String status;
    private Integer duplicateCount;
}
