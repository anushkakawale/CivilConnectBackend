package com.example.CivicConnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.profiles.WardChangeRequest;

public interface WardChangeRequestRepository
        extends JpaRepository<WardChangeRequest, Long> {
}
