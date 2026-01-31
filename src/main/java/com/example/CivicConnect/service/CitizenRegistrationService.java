package com.example.CivicConnect.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.CitizenRegistrationDTO;
import com.example.CivicConnect.dto.CitizenRegistrationResponseDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.exception.DuplicateResourceException;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.repository.WardRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public CitizenRegistrationResponseDTO registerCitizen(
            CitizenRegistrationDTO dto) {

        log.info("Registering citizen with email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Email already exists: {}", dto.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }

        if (userRepository.existsByMobile(dto.getMobile())) {
            log.warn("Mobile already exists: {}", dto.getMobile());
            throw new DuplicateResourceException("Mobile already registered");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(RoleName.CITIZEN);
        user.setActive(true);

        user = userRepository.save(user);
        log.info("User created with ID: {}", user.getUserId());

        CitizenProfile profile = new CitizenProfile();
        profile.setUser(user);

        if (dto.getWardId() != null) {
            Ward ward = wardRepository
                    .findById(dto.getWardId())
                    .orElse(null);

            if (ward != null) {
                profile.setWard(ward);
                log.info("Citizen linked to ward {}", ward.getWardNumber());
            } else {
                log.warn("Ward {} not found, skipping", dto.getWardId());
            }
        }

        citizenProfileRepository.save(profile);

        return new CitizenRegistrationResponseDTO(
                user.getUserId(),
                user.getName(),
                "Citizen registered successfully"
        );
    }
}
