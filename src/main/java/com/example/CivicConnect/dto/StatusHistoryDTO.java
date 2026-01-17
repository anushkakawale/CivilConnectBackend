package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.Data;

@Data
public class StatusHistoryDTO {

    private ComplaintStatus status;
    private LocalDateTime changedAt;
    private String changedBy;
}
