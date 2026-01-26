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
import com.example.CivicConnect.service.system.AccessLogService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class WardOfficerRegistrationService {

    private final UserRepository userRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final WardRepository wardRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AccessLogService accessLogService;

    public WardOfficerRegistrationService(
            UserRepository userRepository,
            OfficerProfileRepository officerProfileRepository,
            WardRepository wardRepository,
            PasswordEncoder passwordEncoder,
            JWTService jwtService,
            AccessLogService accessLogService) {

        this.userRepository = userRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.wardRepository = wardRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accessLogService = accessLogService;
    }

    public Map<String, Object> registerWardOfficer(
            WardOfficerRegistrationDTO dto,
            User adminUser) {

        if (adminUser.getRole() != RoleName.ADMIN) {
            throw new RuntimeException("Only ADMIN can create Ward Officers");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 1️⃣ Create User
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(RoleName.WARD_OFFICER);
        user.setActive(true);
        userRepository.save(user);

        // 2️⃣ Officer Profile
        OfficerProfile profile = new OfficerProfile();
        profile.setUser(user);
        profile.setWard(
                wardRepository.findById(dto.getWardId())
                        .orElseThrow(() -> new RuntimeException("Ward not found"))
        );
        profile.setActive(true);
        profile.setActiveComplaintCount(0);
        officerProfileRepository.save(profile);

        // 3️⃣ AUDIT LOG (🔥 IMPORTANT)
        accessLogService.log(
                adminUser,
                "CREATE_WARD_OFFICER",
                "USER",
                user.getUserId(),
                "SYSTEM"
        );

        // 4️⃣ JWT (optional but OK)
        String token = jwtService.generateToken(user);

        return Map.of(
                "message", "Ward Officer registered successfully",
                "wardOfficerId", user.getUserId(),
                "role", user.getRole().name()
        );
    }
}
