package com.example.CivicConnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.profiles.CitizenProfile;

public interface CitizenProfileRepository
extends JpaRepository<CitizenProfile, Long> {
	Optional<CitizenProfile> findByUser_UserId(Long userId);
}
