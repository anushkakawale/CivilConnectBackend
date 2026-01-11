package com.example.CivicConnect.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.DepartmentOfficerRegistrationDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.DepartmentRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.repository.WardRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DepartmentOfficerRegistrationService {

    private final UserRepository userRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final WardRepository wardRepository;
    private final PasswordEncoder passwordEncoder;

    public DepartmentOfficerRegistrationService(
            UserRepository userRepository,
            OfficerProfileRepository officerProfileRepository,
            DepartmentRepository departmentRepository,
            WardRepository wardRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.departmentRepository = departmentRepository;
        this.wardRepository = wardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerDepartmentOfficer(DepartmentOfficerRegistrationDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByMobile(dto.getMobile())) {
            throw new RuntimeException("Mobile already exists");
        }

        // 1️ USER
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(RoleName.DEPARTMENT_OFFICER);
        user.setActive(true);

        userRepository.save(user);

        // 2️ OFFICER_PROFILE
        OfficerProfile profile = new OfficerProfile();
        profile.setUser(user);

        profile.setDepartment(
                departmentRepository.findById(dto.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found"))
        );

        profile.setWard(
                wardRepository.findById(dto.getWardId())
                        .orElseThrow(() -> new RuntimeException("Ward not found"))
        );

        profile.setActive(true);
        profile.setActiveComplaintCount(0);

        officerProfileRepository.save(profile);
    }
}
