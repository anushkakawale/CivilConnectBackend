package com.example.CivicConnect.dto;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComplaintMapDTO {
    private Long complaintId;
    private double latitude;
    private double longitude;
    private ComplaintStatus status;
//    private String wardName;
//    private String departmentName;
}