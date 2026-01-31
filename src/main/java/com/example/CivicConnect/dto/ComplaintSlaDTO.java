package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.SLAStatus;

public record ComplaintSlaDTO(
	    Long complaintId,
	    SLAStatus status,
	    LocalDateTime deadline,
	    boolean escalated
	) {}