package com.example.CivicConnect.service;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.DepartmentOfficerRegistrationDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.geography.Department;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.DepartmentRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.repository.WardRepository;
import com.example.CivicConnect.service.citizencomplaint.ComplaintAssignmentService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DepartmentOfficerRegistrationService {

	private final UserRepository userRepository;
	private final OfficerProfileRepository officerProfileRepository;
	private final WardRepository wardRepository;
	private final DepartmentRepository departmentRepository;
	private final PasswordEncoder passwordEncoder;
	private final ComplaintAssignmentService complaintAssignmentService; // ✅
	private final JWTService jwtService;

	public DepartmentOfficerRegistrationService(UserRepository userRepository,
			OfficerProfileRepository officerProfileRepository, WardRepository wardRepository,
			DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder,
			ComplaintAssignmentService complaintAssignmentService, JWTService jwtService) { // ✅ ADD THIS

		this.userRepository = userRepository;
		this.officerProfileRepository = officerProfileRepository;
		this.wardRepository = wardRepository;
		this.departmentRepository = departmentRepository;
		this.passwordEncoder = passwordEncoder;
		this.complaintAssignmentService = complaintAssignmentService;
		this.jwtService = jwtService; // ✅ ADD THIS
	}

	public Map<String, Object> registerDepartmentOfficer(
            DepartmentOfficerRegistrationDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByMobile(dto.getMobile())) {
            throw new RuntimeException("Mobile already exists");
        }

        // 1️⃣ USER
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(RoleName.DEPARTMENT_OFFICER);
        user.setActive(true);

        userRepository.save(user);

        // 2️⃣ OFFICER PROFILE
        OfficerProfile profile = new OfficerProfile();
        profile.setUser(user);

        Ward ward = wardRepository.findById(dto.getWardId())
                .orElseThrow(() -> new RuntimeException("Ward not found"));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        profile.setWard(ward);
        profile.setDepartment(department);
        profile.setActive(true);
        profile.setActiveComplaintCount(0);

        officerProfileRepository.save(profile);

        // 3️⃣ AUTO-ASSIGN PENDING COMPLAINTS
        complaintAssignmentService.assignPendingComplaintsForOfficer(profile);

        // ✅ 4️⃣ GENERATE TOKEN
        String token = jwtService.generateToken(user);

        // ✅ 5️⃣ RETURN RESPONSE
        return Map.of(
                "message", "Department Officer registered successfully",
                "token", token,
                "role", user.getRole().name()
        );
    }
}