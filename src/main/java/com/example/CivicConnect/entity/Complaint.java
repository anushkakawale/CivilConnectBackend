package com.example.CivicConnect.entity;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Complaint {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complaintId;
	
	private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;
    
    private int duplicateCount;
    private LocalDateTime createdAt;

    @ManyToOne
    private User citizen;

    @ManyToOne
    private User officer;

    @ManyToOne
    private Department department;

    @ManyToOne
    private Ward ward;
}
