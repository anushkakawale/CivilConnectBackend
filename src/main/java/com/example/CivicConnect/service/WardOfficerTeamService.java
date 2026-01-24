package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.hierarchy.OfficerHierarchy;
import com.example.CivicConnect.repository.OfficerHierarchyRepository;

@Service
public class WardOfficerTeamService {

    private final OfficerHierarchyRepository hierarchyRepository;

    public WardOfficerTeamService(
            OfficerHierarchyRepository hierarchyRepository) {
        this.hierarchyRepository = hierarchyRepository;
    }

    public List<User> getDepartmentOfficers(Long wardOfficerUserId) {

        return hierarchyRepository
                .findByWardOfficer_UserId(wardOfficerUserId)
                .stream()
                .map(OfficerHierarchy::getDepartmentOfficer)
                .toList();
    }
}
