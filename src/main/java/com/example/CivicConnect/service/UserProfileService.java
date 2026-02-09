package com.example.CivicConnect.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.AdminProfileResponseDTO;
import com.example.CivicConnect.dto.OfficerProfileResponseDTO;
import com.example.CivicConnect.dto.PasswordUpdateDTO;
import com.example.CivicConnect.dto.ProfileResponseDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.AdminProfileRepository;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.UserRepository;

@Service
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    
    @org.springframework.beans.factory.annotation.Value("${file.upload.dir}")
    private String uploadDir;

    public UserProfileService(UserRepository userRepository,
                              CitizenProfileRepository citizenProfileRepository,
                              OfficerProfileRepository officerProfileRepository,
                              AdminProfileRepository adminProfileRepository,
                              PasswordEncoder passwordEncoder,
                              NotificationService notificationService) {
        this.userRepository = userRepository;
        this.citizenProfileRepository = citizenProfileRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public AdminProfileResponseDTO getAdminProfile(User user) {
        return AdminProfileResponseDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }

    public OfficerProfileResponseDTO getOfficerProfile(User user) {
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        return OfficerProfileResponseDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .department(profile.getDepartment() != null ? profile.getDepartment().getName() : "N/A")
                .ward(profile.getWard() != null ? profile.getWard().getAreaName() : "N/A")
                .activeComplaintCount(profile.getActiveComplaintCount())
                .active(profile.isActive())
                .build();
    }
    
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
        dto.setProfileImage(dbUser.getProfileImage());
        dto.setActive(dbUser.isActive());
        dto.setMemberSince(dbUser.getCreatedAt() != null ? dbUser.getCreatedAt().toLocalDate().toString() : "N/A");
        dto.setLastLogin(dbUser.getLastLoginAt() != null ? dbUser.getLastLoginAt().toString() : "Never");

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
                    dto.setAddressLine1(profile.getAddressLine1());
                    dto.setAddressLine2(profile.getAddressLine2());
                    dto.setCity(profile.getCity());
                    dto.setPincode(profile.getPincode());
                });
        }

        // 🧑‍✈️ WARD OFFICER & DEPARTMENT OFFICER PROFILE
        if (dbUser.getRole() == RoleName.WARD_OFFICER || dbUser.getRole() == RoleName.DEPARTMENT_OFFICER) {
            officerProfileRepository
                .findByUser_UserId(dbUser.getUserId())
                .ifPresent(profile -> {
                    // Populate Ward info
                    if (profile.getWard() != null) {
                        dto.setWardId(profile.getWard().getWardId());
                        dto.setWardNumber(profile.getWard().getWardNumber());
                        dto.setAreaName(profile.getWard().getAreaName());
                        dto.setWard(profile.getWard().getAreaName());
                    }
                    // Populate Department info (for Department Officers)
                    if (profile.getDepartment() != null) {
                        dto.setDepartmentId(profile.getDepartment().getDepartmentId());
                        dto.setDepartmentName(profile.getDepartment().getName());
                        dto.setDepartment(profile.getDepartment().getName());
                    }
                    // Populate common officer info
                    dto.setDesignation(profile.getDesignation());
                    dto.setEmployeeId(profile.getEmployeeId());
                });
        }

        // 🛡 ADMIN → user-only (no ward)
        
        dto.setCompletionScore(calculateCompletionScore(dbUser));

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

    // ===============================
    // CALCULATE COMPLETION SCORE
    // ===============================
    public int calculateCompletionScore(User user) {
        int score = 0;
        
        RoleName role = user.getRole();
        
        if (role == RoleName.CITIZEN) {
            // 1. Base User Fields (40%)
            if (user.getName() != null && !user.getName().isEmpty()) score += 10;
            if (user.getMobile() != null && !user.getMobile().isEmpty()) score += 10;
            if (user.getEmail() != null && !user.getEmail().isEmpty()) score += 10;
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) score += 10;

            // 2. Citizen Specifics (60%)
            // Ward (30%)
            // Address (30%)
            final int[] extra = {0};
            citizenProfileRepository.findByUser_UserId(user.getUserId()).ifPresent(p -> {
                if (p.getWard() != null) extra[0] += 30;
                
                boolean hasAddress = p.getAddressLine1() != null && !p.getAddressLine1().isEmpty();
                boolean hasCity = p.getCity() != null && !p.getCity().isEmpty();
                
                if (hasAddress && hasCity) {
                    extra[0] += 30;
                } else if (hasAddress || hasCity) {
                    extra[0] += 15;
                }
            });
            score += extra[0];
            
        } else {
            // OFFICERS (Ward/Dept) & ADMIN
            // Logic: Officers cannot edit Ward/Dept, so their completion should be 100% based on their controllable profile.
            // 4 Fields = 25% each
            if (user.getName() != null && !user.getName().isEmpty()) score += 25;
            if (user.getMobile() != null && !user.getMobile().isEmpty()) score += 25;
            if (user.getEmail() != null && !user.getEmail().isEmpty()) score += 25;
            // For officers/admin, image is optional but good, let's say it makes up the last 25%
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) score += 25;
        }
        
        return Math.min(score, 100);
    }

    // ===============================
    // UPDATE PROFILE IMAGE
    // ===============================
    public String updateProfileImage(User user, org.springframework.web.multipart.MultipartFile file) {
        try {
            // 1. Validate
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }
            
            // 2. Create Directory
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir).resolve("profiles");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            
            // 3. Generate Filename (userid_timestamp_original)
            // Sanitize filename to avoid issues
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) originalFilename = "profile.jpg";
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            String filename = user.getUserId() + "_" + System.currentTimeMillis() + "_" + safeFilename;
            
            java.nio.file.Path filePath = uploadPath.resolve(filename);
            
            // 4. Save
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // 5. Update User
            // Build URL compatible with ResourceHandler
            String fileUrl = "/uploads/profiles/" + filename;
            
            user.setProfileImage(fileUrl);
            userRepository.save(user);
            
            return fileUrl;
            
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }
}
