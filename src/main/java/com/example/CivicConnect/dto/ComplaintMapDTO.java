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
    private double latitude;
    private double longitude;
    private ComplaintStatus status;
    private SLAStatus slaStatus;

//    private String wardName;
//    private String departmentName;
}