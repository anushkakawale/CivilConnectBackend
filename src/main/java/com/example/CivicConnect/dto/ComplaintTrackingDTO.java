package com.example.CivicConnect.dto;

import java.util.List;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintTrackingDTO {

    private Long complaintId;
    private String title;
    private String description;
    private ComplaintStatus currentStatus;

    // Officer details
    private String officerName;
    private String officerMobile;

    private List<StatusHistoryDTO> history;
}
