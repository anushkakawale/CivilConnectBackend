package com.example.CivicConnect.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.OfficerProfileUpdateDTO;
import com.example.CivicConnect.entity.geography.Department;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.DepartmentRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.WardRepository;

@Service
@Transactional
public class OfficerProfileService {

    private final OfficerProfileRepository officerProfileRepository;
    private final WardRepository wardRepository;
    private final DepartmentRepository departmentRepository;

    public OfficerProfileService(
            OfficerProfileRepository officerProfileRepository,
            WardRepository wardRepository,
            DepartmentRepository departmentRepository) {

        this.officerProfileRepository = officerProfileRepository;
        this.wardRepository = wardRepository;
        this.departmentRepository = departmentRepository;
    }

    public void updateProfile(Long userId, OfficerProfileUpdateDTO dto) {

        OfficerProfile profile =
                officerProfileRepository.findByUser_UserId(userId)
                        .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (dto.getWardId() != null) {
            Ward ward = wardRepository.findById(dto.getWardId())
                    .orElseThrow(() -> new RuntimeException("Ward not found"));
            profile.setWard(ward);
        }

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            profile.setDepartment(dept);
        }

        officerProfileRepository.save(profile);
    }
}
