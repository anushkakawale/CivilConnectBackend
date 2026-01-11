package com.example.CivicConnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.profiles.OfficerProfile;

public interface OfficerProfileRepository
extends JpaRepository<OfficerProfile, Long> {
}

