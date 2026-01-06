package com.example.CivicConnect.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ComplaintUpdate {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long updateId;

    @ManyToOne
    private Complaint complaint;

    @ManyToOne
    private User officer;

    private String message;
    
    private LocalDateTime updatedAt;
}
