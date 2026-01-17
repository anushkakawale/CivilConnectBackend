package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.Data;
@Data
public class ComplaintSummaryDTO {

    private Long complaintId;
    private String title;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
}
