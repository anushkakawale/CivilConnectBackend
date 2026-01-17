package com.example.CivicConnect.dto;

import java.util.List;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.Data;

@Data
public class ComplaintTrackingDTO {

    private Long complaintId;
    private String title;
    private String description;
    private ComplaintStatus currentStatus;
    private List<StatusHistoryDTO> history;
}

