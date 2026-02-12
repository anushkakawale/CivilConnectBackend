package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalQueueDTO {
    private Long id;
    private String title;
    private String departmentName;
    private String priority;
    private String resolvedBy;
    private String resolvedRemarks;
    private String slaStatus;
    private LocalDateTime resolvedAt;
}
