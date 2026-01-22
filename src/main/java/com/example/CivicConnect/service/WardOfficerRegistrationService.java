package com.example.CivicConnect.service;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.WardOfficerRegistrationDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.repository.WardRepository;
import com.example.CivicConnect.service.citizencomplaint.ComplaintAssignmentService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class WardOfficerRegistrationService {

    private final UserRepository userRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final WardRepository wardRepository;
    private final PasswordEncoder passwordEncoder;
    private final ComplaintAssignmentService complaintAssignmentService;
    private final JWTService jwtService; // ✅ ADD THIS

    public WardOfficerRegistrationService(
            UserRepository userRepository,
            OfficerProfileRepository officerProfileRepository,
            WardRepository wardRepository,
            PasswordEncoder passwordEncoder,
            ComplaintAssignmentService complaintAssignmentService,
            JWTService jwtService) { // ✅ ADD THIS

        this.userRepository = userRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.wardRepository = wardRepository;
        this.passwordEncoder = passwordEncoder;
        this.complaintAssignmentService = complaintAssignmentService;
        this.jwtService = jwtService;
    }

    public Map<String, Object> registerWardOfficer(WardOfficerRegistrationDTO dto) {

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
        user.setRole(RoleName.WARD_OFFICER);
        user.setActive(true);

        userRepository.save(user);

        // 2️⃣ OFFICER PROFILE
        OfficerProfile profile = new OfficerProfile();
        profile.setUser(user);
        profile.setWard(
                wardRepository.findById(dto.getWardId())
                        .orElseThrow(() -> new RuntimeException("Ward not found"))
        );
        profile.setActive(true);
        profile.setActiveComplaintCount(0);

        officerProfileRepository.save(profile);

        // ✅ 3️⃣ GENERATE TOKEN
        String token = jwtService.generateToken(user);

        // ✅ 4️⃣ RETURN RESPONSE
        return Map.of(
                "message", "Ward Officer registered successfully",
                "token", token,
                "role", user.getRole().name()
        );
    }
}
