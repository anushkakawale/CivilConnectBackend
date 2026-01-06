package com.example.CivicConnect.entity.profiles;

import com.example.CivicConnect.entity.User;
import com.example.CivicConnect.entity.Ward;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

public class CitizenProfile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long citizenProfileId;
	
	@OneToOne
	private User user;
	
	@ManyToOne
	private Ward ward;
}
