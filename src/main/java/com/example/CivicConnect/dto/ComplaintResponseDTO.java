package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComplaintResponseDTO {
	private Long complaintId;
    private String status;
    private int duplicateCount;
    private String message;

}
