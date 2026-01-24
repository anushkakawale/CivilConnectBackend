package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryDTO {

    private ComplaintStatus status;
    private LocalDateTime changedAt;
    private String changedBy;
}
