package com.example.CivicConnect.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.PasswordUpdateDTO;
import com.example.CivicConnect.dto.ProfileResponseDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileService {

	private final UserRepository userRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    // ===============================
    // VIEW PROFILE (COMMON – ALL ROLES)
    // ===============================
    public ProfileResponseDTO getProfile(User user) {

        User dbUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setUserId(dbUser.getUserId());
        dto.setName(dbUser.getName());
        dto.setEmail(dbUser.getEmail());
        dto.setMobile(dbUser.getMobile());
        dto.setRole(dbUser.getRole().name());

        // 🧑‍💼 CITIZEN PROFILE
        if (dbUser.getRole() == RoleName.CITIZEN) {
            citizenProfileRepository
                .findByUser_UserId(dbUser.getUserId())
                .ifPresent(profile -> {
                    if (profile.getWard() != null) {
                        dto.setWardId(profile.getWard().getWardId());
                        dto.setWardNumber(profile.getWard().getWardNumber());
                        dto.setAreaName(profile.getWard().getAreaName());
                    }
                });
        }

        // 🧑‍✈️ WARD OFFICER & DEPARTMENT OFFICER PROFILE
        if (dbUser.getRole() == RoleName.WARD_OFFICER || dbUser.getRole() == RoleName.DEPARTMENT_OFFICER) {
            officerProfileRepository
                .findByUser_UserId(dbUser.getUserId())
                .ifPresent(profile -> {
                    // Populate Ward info if present (for Ward Officers)
                    if (profile.getWard() != null) {
                        dto.setWardId(profile.getWard().getWardId());
                        dto.setWardNumber(profile.getWard().getWardNumber());
                        dto.setAreaName(profile.getWard().getAreaName());
                    }
                    // Populate Department info if present (for Department Officers)
                    if (profile.getDepartment() != null) {
                        dto.setDepartmentId(profile.getDepartment().getDepartmentId());
                        dto.setDepartmentName(profile.getDepartment().getDepartmentName());
                    }
                    // Populate common officer info
                    dto.setDesignation(profile.getDesignation());
                    dto.setEmployeeId(profile.getEmployeeId());
                });
        }

        // 🛡 ADMIN → user-only (no ward)

        return dto;
    }


    // ===============================
    // UPDATE NAME
    // ===============================
    public void updateName(User user, String newName) {

        if (newName == null || newName.trim().isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }

        user.setName(newName.trim());
        userRepository.save(user);

        notificationService.notifyUser(
                user,
                "Profile Updated",
                "Your name has been successfully updated."
        );
    }

    // ===============================
    // UPDATE PASSWORD
    // ===============================
    public void updatePassword(User user, PasswordUpdateDTO dto) {

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        notificationService.notifyUser(
                user,
                "Password Changed",
                "Your password has been successfully changed."
        );
    }
}
