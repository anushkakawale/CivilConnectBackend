package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintSummaryDTO {

    private Long complaintId;
    private String title;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
}
