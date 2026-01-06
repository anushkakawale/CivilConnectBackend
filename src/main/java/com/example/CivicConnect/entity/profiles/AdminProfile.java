package com.example.CivicConnect.entity.profiles;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class AdminProfile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long adminProfileId;
	
	@OneToOne
	private User user;
	
	private LocalDateTime createdAt;
	
}
