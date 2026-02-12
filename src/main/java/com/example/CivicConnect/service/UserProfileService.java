package com.example.CivicConnect.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.AdminProfileResponseDTO;
import com.example.CivicConnect.dto.OfficerProfileResponseDTO;
import com.example.CivicConnect.dto.PasswordUpdateDTO;
import com.example.CivicConnect.dto.ProfileResponseDTO;
import com.example.CivicConnect.entity.core.Address;
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
                        dto.setWardName(profile.getWard().getAreaName()); // Populate wardName for frontend
                        dto.setWard(profile.getWard().getAreaName()); // Alias
                    }
                    if (profile.getAddress() != null) {
                        Address addr = profile.getAddress();
                        dto.setAddress(addr.getFullDisplayAddress());
                        dto.setAddressLine1(addr.getAddressLine1());
                        dto.setAddressLine2(addr.getAddressLine2());
                        dto.setCity(addr.getCity());
                        dto.setPincode(addr.getPincode());
                    }
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
                        dto.setWardName(profile.getWard().getAreaName()); // Populate wardName
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
    /**
     * Calculates profile completion score based on role-specific editable fields.
     * 
     * CITIZEN (100% = 6 fields):
     *   - Name (15%)
     *   - Email (15%)
     *   - Mobile (15%)
     *   - Profile Image (10%)
     *   - Ward (25%)
     *   - Address (20% - requires both addressLine1 and city)
     * 
     * OFFICER/ADMIN (100% = 4 fields - cannot edit ward/department):
     *   - Name (25%)
     *   - Email (25%)
     *   - Mobile (25%)
     *   - Profile Image (25%)
     */
    public int calculateCompletionScore(User user) {
        int score = 0;
        
        RoleName role = user.getRole();
        
        if (role == RoleName.CITIZEN) {
            // CITIZEN: 6 editable fields
            // Core fields (55%)
            if (user.getName() != null && !user.getName().trim().isEmpty()) score += 20;
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) score += 20;
            if (user.getMobile() != null && !user.getMobile().trim().isEmpty()) score += 20;

            // Citizen-specific fields (45%)
            final int[] extra = {0};
            citizenProfileRepository.findByUser_UserId(user.getUserId()).ifPresent(profile -> {
                // Ward (25%)
                if (profile.getWard() != null) {
                    extra[0] += 25;
                }
                
                // Address (20%) - requires both addressLine1 and city for full score
                if (profile.getAddress() != null) {
                    com.example.CivicConnect.entity.core.Address addr = profile.getAddress();
                    boolean hasAddressLine1 = addr.getAddressLine1() != null && !addr.getAddressLine1().trim().isEmpty();
                    boolean hasCity = addr.getCity() != null && !addr.getCity().trim().isEmpty();
                    boolean hasPincode = addr.getPincode() != null && !addr.getPincode().trim().isEmpty();
                    
                    if (hasAddressLine1 && hasCity && hasPincode) {
                        extra[0] += 20; // Full address
                    } else if (hasAddressLine1 && hasCity) {
                        extra[0] += 15; // Address without pincode
                    } else if (hasAddressLine1 || hasCity) {
                        extra[0] += 8; // Partial address
                    }
                }
            });
            score += extra[0];
            
        } else if (role == RoleName.WARD_OFFICER || role == RoleName.DEPARTMENT_OFFICER) {
            // OFFICERS: 4 editable fields (ward/department assigned by admin, not editable)
            // Each field = 25%
            if (user.getName() != null && !user.getName().trim().isEmpty()) score += 34;
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) score += 33;
            if (user.getMobile() != null && !user.getMobile().trim().isEmpty()) score += 33;
            
        } else if (role == RoleName.ADMIN) {
            // ADMIN: 4 editable fields
            // Each field = 25%
            if (user.getName() != null && !user.getName().trim().isEmpty()) score += 34;
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) score += 33;
            if (user.getMobile() != null && !user.getMobile().trim().isEmpty()) score += 33;
        }
        
        return Math.min(score, 100);
    }
    
    /**
     * Get detailed completion breakdown for frontend display
     */
    public java.util.Map<String, Object> getCompletionBreakdown(User user) {
        java.util.Map<String, Object> breakdown = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> fields = new java.util.ArrayList<>();
        
        RoleName role = user.getRole();
        int totalScore = 0;
        
        if (role == RoleName.CITIZEN) {
            // Core fields
            fields.add(createFieldStatus("Name", user.getName() != null && !user.getName().trim().isEmpty(), 20, true));
            fields.add(createFieldStatus("Email", user.getEmail() != null && !user.getEmail().trim().isEmpty(), 20, true));
            fields.add(createFieldStatus("Mobile", user.getMobile() != null && !user.getMobile().trim().isEmpty(), 20, true));
            
            // Citizen-specific fields
            citizenProfileRepository.findByUser_UserId(user.getUserId()).ifPresent(profile -> {
                boolean hasWard = profile.getWard() != null;
                fields.add(createFieldStatus("Ward", hasWard, 25, true));
                
                if (profile.getAddress() != null) {
                    com.example.CivicConnect.entity.core.Address addr = profile.getAddress();
                    boolean hasAddressLine1 = addr.getAddressLine1() != null && !addr.getAddressLine1().trim().isEmpty();
                    boolean hasCity = addr.getCity() != null && !addr.getCity().trim().isEmpty();
                    boolean hasPincode = addr.getPincode() != null && !addr.getPincode().trim().isEmpty();
                    
                    int addressScore = 0;
                    if (hasAddressLine1 && hasCity && hasPincode) addressScore = 20;
                    else if (hasAddressLine1 && hasCity) addressScore = 15;
                    else if (hasAddressLine1 || hasCity) addressScore = 8;
                    
                    fields.add(createFieldStatus("Address", hasAddressLine1 && hasCity, addressScore, true));
                } else {
                    fields.add(createFieldStatus("Address", false, 0, true));
                }
            });
            
        } else {
            // Officers and Admin - only editable fields
            fields.add(createFieldStatus("Name", user.getName() != null && !user.getName().trim().isEmpty(), 34, true));
            fields.add(createFieldStatus("Email", user.getEmail() != null && !user.getEmail().trim().isEmpty(), 33, true));
            fields.add(createFieldStatus("Mobile", user.getMobile() != null && !user.getMobile().trim().isEmpty(), 33, true));
            
            // Add non-editable fields for display (not counted in score)
            if (role == RoleName.WARD_OFFICER || role == RoleName.DEPARTMENT_OFFICER) {
                officerProfileRepository.findByUser_UserId(user.getUserId()).ifPresent(profile -> {
                    if (profile.getWard() != null) {
                        fields.add(createFieldStatus("Ward", true, 0, false));
                    }
                    if (profile.getDepartment() != null) {
                        fields.add(createFieldStatus("Department", true, 0, false));
                    }
                    if (profile.getDesignation() != null && !profile.getDesignation().trim().isEmpty()) {
                        fields.add(createFieldStatus("Designation", true, 0, false));
                    }
                    if (profile.getEmployeeId() != null && !profile.getEmployeeId().trim().isEmpty()) {
                        fields.add(createFieldStatus("Employee ID", true, 0, false));
                    }
                });
            }
        }
        
        totalScore = calculateCompletionScore(user);
        
        breakdown.put("totalScore", totalScore);
        breakdown.put("fields", fields);
        breakdown.put("role", role.name());
        
        return breakdown;
    }
    
    private java.util.Map<String, Object> createFieldStatus(String fieldName, boolean completed, int weight, boolean editable) {
        java.util.Map<String, Object> field = new java.util.HashMap<>();
        field.put("name", fieldName);
        field.put("completed", completed);
        field.put("weight", weight);
        field.put("editable", editable);
        return field;
    }

}
