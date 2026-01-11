package com.example.CivicConnect.dto.citizen;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CitizenProfileResponseDTO {
	private String name;
	private String email;
	private String mobile;
	private String wardNumber;
	private String areaName;
	private LocalDateTime createdAt;
	public void setUserId(Long userId) {
		// TODO Auto-generated method stub
		
	}
}
