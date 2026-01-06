package com.example.CivicConnect.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class UserRole {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userRoleId;
	
	@ManyToOne
	private User user; //Many UserRole rows → One User
	
	@ManyToOne
	private Role role; //Many UserRole rows → One Role
	
	private boolean active;
	
	private LocalDateTime assignedAt;
	
	private LocalDateTime revokedAt;
}
