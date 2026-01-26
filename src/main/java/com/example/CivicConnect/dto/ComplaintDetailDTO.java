package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComplaintDetailDTO {

    private Long complaintId;
    private String title;
    private String description;
    private ComplaintStatus status;

    private String officerName;
    private String officerMobile;

    private LocalDateTime createdAt;

    private List<StatusHistoryDTO> history;
}
