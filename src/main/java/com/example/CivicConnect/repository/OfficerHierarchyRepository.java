package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.hierarchy.OfficerHierarchy;

public interface OfficerHierarchyRepository
        extends JpaRepository<OfficerHierarchy, Long> {

    // Ward officer → all department officers under them
    List<OfficerHierarchy> findByWardOfficer_UserId(Long wardOfficerId);
}
