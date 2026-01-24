package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.OfficerProfileRepository;

@Service
public class AdminOfficerService {

    private final OfficerProfileRepository officerProfileRepository;

    public AdminOfficerService(OfficerProfileRepository officerProfileRepository) {
        this.officerProfileRepository = officerProfileRepository;
    }

    public List<OfficerProfile> getAllWardOfficers() {
        return officerProfileRepository.findByUser_Role(RoleName.WARD_OFFICER);
    }

    public List<OfficerProfile> getAllDepartmentOfficers() {
        return officerProfileRepository.findByUser_Role(RoleName.DEPARTMENT_OFFICER);
    }
}
