package com.example.CivicConnect.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	@Column(unique=true)
	private String email;
	
	@Column(unique=true)
	private String mobile;
	
	private String password;
	
	private boolean active = true;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime lastLoginAt;
}
