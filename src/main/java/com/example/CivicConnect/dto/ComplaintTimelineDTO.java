package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComplaintTimelineDTO {
    private String status;
    private String changedBy;
    private LocalDateTime changedAt;
}
