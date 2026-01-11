package com.example.CivicConnect.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.CitizenRegistrationDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.repository.WardRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CitizenRegistrationService {

    private final UserRepository userRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final WardRepository wardRepository;
    private final PasswordEncoder passwordEncoder;

    public CitizenRegistrationService(
            UserRepository userRepository,
            CitizenProfileRepository citizenProfileRepository,
            WardRepository wardRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.citizenProfileRepository = citizenProfileRepository;
        this.wardRepository = wardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerCitizen(CitizenRegistrationDTO dto) {

        // 1️ Duplicate checks
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByMobile(dto.getMobile())) {
            throw new RuntimeException("Mobile already registered");
        }

        // 2️ Create USER
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(RoleName.CITIZEN);
        user.setActive(true);

        userRepository.save(user);

        // 3️ Create CITIZEN_PROFILE
        CitizenProfile profile = new CitizenProfile();
        profile.setUser(user);

        // ward is OPTIONAL – SAFE handling
        if (dto.getWardId() != null) {
            wardRepository.findById(dto.getWardId())
                    .ifPresent(profile::setWard);
        }

        citizenProfileRepository.save(profile);
    }
}
