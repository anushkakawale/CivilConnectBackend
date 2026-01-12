package com.example.CivicConnect.dto;

import lombok.Data;

@Data
public class ComplaintRequestDTO {
	private Long citizenUserId;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private Long departmentId;
}
