package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.OfficerDirectoryDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfficerDirectoryService {

    private final OfficerProfileRepository officerProfileRepository;
    private final CitizenProfileRepository citizenProfileRepository;

    // ===================================
    // 👤 CITIZEN: All officers of his ward
    // ===================================
    public List<OfficerDirectoryDTO> getOfficersForCitizen(User citizen) {
        validateRole(citizen, RoleName.CITIZEN);
        Long wardId = getCitizenWardIdOrNull(citizen);
        if (wardId == null) return List.of();

        return officerProfileRepository.findByWard_WardId(wardId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ===================================
    // 👤 CITIZEN: Ward Officer only
    // ===================================
    public OfficerDirectoryDTO getWardOfficerForCitizen(User citizen) {
        validateRole(citizen, RoleName.CITIZEN);
        Long wardId = getCitizenWardIdOrNull(citizen);
        if (wardId == null) return null;

        return officerProfileRepository
                .findByWard_WardIdAndUser_Role(wardId, RoleName.WARD_OFFICER)
                .stream()
                .findFirst()
                .map(this::toDto)
                .orElse(null); // Return null instead of crashing
    }

    // ===================================
    // 👤 CITIZEN: Department Officers of ward
    // ===================================
    public List<OfficerDirectoryDTO> getDepartmentOfficersForCitizen(User citizen) {
        validateRole(citizen, RoleName.CITIZEN);
        Long wardId = getCitizenWardIdOrNull(citizen);
        if (wardId == null) return List.of();

        return officerProfileRepository
                .findByWard_WardIdAndUser_Role(wardId, RoleName.DEPARTMENT_OFFICER)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ===================================
    // 👤 CITIZEN: ALL Dept Officers (City-wide)
    // ===================================
    public List<OfficerDirectoryDTO> getAllDepartmentOfficersForCitizen(User citizen) {
        validateRole(citizen, RoleName.CITIZEN);
        return officerProfileRepository.findByUser_Role(RoleName.DEPARTMENT_OFFICER)
                .stream()
                .filter(p -> p.getUser().isActive())
                .map(this::toDto)
                .toList();
    }

    // =================================================
    // 🏢 DEPARTMENT OFFICER: His Ward Officer
    // =================================================
    public OfficerDirectoryDTO getWardOfficerForDepartmentOfficer(User officer) {

        validateRole(officer, RoleName.DEPARTMENT_OFFICER);

        OfficerProfile profile = getOfficerProfile(officer);

        return officerProfileRepository
                .findByWard_WardIdAndUser_Role(
                        profile.getWard().getWardId(),
                        RoleName.WARD_OFFICER
                )
                .stream()
                .findFirst()
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Ward officer not found"));
    }

    // =================================================
    // 🏘 WARD OFFICER: Department Officers of ward
    // =================================================
    public List<OfficerDirectoryDTO> getDepartmentOfficersForWardOfficer(User wardOfficer) {

        validateRole(wardOfficer, RoleName.WARD_OFFICER);

        OfficerProfile profile = getOfficerProfile(wardOfficer);

        return officerProfileRepository
                .findByWard_WardIdAndUser_Role(
                        profile.getWard().getWardId(),
                        RoleName.DEPARTMENT_OFFICER
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =================================================
    // =================================================
    // 🏢 DEPARTMENT OFFICER: Peers (Same Ward & Department)
    // =================================================
    public List<OfficerDirectoryDTO> getPeersForDepartmentOfficer(User officer) {
        validateRole(officer, RoleName.DEPARTMENT_OFFICER);
        OfficerProfile profile = getOfficerProfile(officer);
        
        if (profile.getWard() == null || profile.getDepartment() == null) {
            return List.of();
        }

        return officerProfileRepository
                .findByWard_WardIdAndDepartment_DepartmentId(
                        profile.getWard().getWardId(),
                        profile.getDepartment().getDepartmentId()
                )
                .stream()
                .filter(p -> !p.getUser().getUserId().equals(officer.getUserId()))
                .map(this::toDto)
                .toList();
    }

    // =================================================
    // 🛡 ADMIN: All officers
    // =================================================
    public List<OfficerDirectoryDTO> getAllOfficersForAdmin(User admin) {

        validateRole(admin, RoleName.ADMIN);

        return officerProfileRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<com.example.CivicConnect.dto.AdminOfficerDTO> getAllOfficersForAdminDTO(User admin) {
        validateRole(admin, RoleName.ADMIN);
        return officerProfileRepository.findAll().stream()
            .map(p -> new com.example.CivicConnect.dto.AdminOfficerDTO(
                p.getUser().getUserId(),
                p.getUser().getName(),
                p.getUser().getRole().name(),
                p.getWard() != null ? p.getWard().getAreaName() : "-",
                p.getDepartment() != null ? p.getDepartment().getName() : "-",
                p.getUser().getEmail(),
                p.getUser().getMobile(),
                p.getUser().isActive(),
                p.getUser().getCreatedAt() != null ? p.getUser().getCreatedAt().toString() : "N/A"
            ))
            .toList();
    }

    public OfficerDirectoryDTO getOfficerDetails(Long officerUserId) {
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(officerUserId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        return toDto(profile);
    }

    // =================================================
    // 🔒 HELPERS
    // =================================================
    private void validateRole(User user, RoleName role) {
        if (user.getRole() != role) {
            throw new RuntimeException("Access denied for role: " + user.getRole());
        }
    }

    private OfficerProfile getOfficerProfile(User user) {
        return officerProfileRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
    }

    private Long getCitizenWardId(User citizen) {
        CitizenProfile profile = citizenProfileRepository.findByUser_UserId(citizen.getUserId())
                .orElseThrow(() -> new RuntimeException("Citizen profile not found"));
        if (profile.getWard() == null) {
            throw new RuntimeException("Ward not assigned to your profile");
        }
        return profile.getWard().getWardId();
    }

    private Long getCitizenWardIdOrNull(User citizen) {
        return citizenProfileRepository.findByUser_UserId(citizen.getUserId())
                .map(p -> p.getWard() != null ? p.getWard().getWardId() : null)
                .orElse(null);
    }

    // =================================================
    // DTO MAPPER
    // =================================================
    private OfficerDirectoryDTO toDto(OfficerProfile p) {
        return new OfficerDirectoryDTO(
                p.getUser().getUserId(),
                p.getUser().getName(),
                p.getUser().getMobile(),
                p.getUser().getEmail(),
                p.getUser().getRole().name(),
                p.getDepartment() != null ? p.getDepartment().getName() : "Ward Office",
                p.getWard() != null ? p.getWard().getWardNumber() : null,
                p.getUser().getLastLoginAt()
        );
    }
}
