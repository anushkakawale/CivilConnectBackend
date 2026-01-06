package com.example.CivicConnect.entity.profiles;

import com.example.CivicConnect.entity.Department;
import com.example.CivicConnect.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class OfficerProfile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long officerProfileId;
	
	@OneToOne
	private User user;
	
	@ManyToOne
	private Department department;
	
	private boolean active;
	
	private int activeComplaintCount;
}
