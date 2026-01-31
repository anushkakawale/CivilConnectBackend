package com.example.CivicConnect.service.citizen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CitizenOfficersService {

    private final OfficerProfileRepository officerProfileRepository;
    private final CitizenProfileRepository citizenProfileRepository;

    public List<Map<String, Object>> getOfficers(User citizen, Long departmentId) {
        // Get citizen's ward
        CitizenProfile citizenProfile = citizenProfileRepository
            .findByUser_UserId(citizen.getUserId())
            .orElseThrow(() -> new RuntimeException("Citizen profile not found"));

        Long wardId = citizenProfile.getWard() != null ? 
            citizenProfile.getWard().getWardId() : null;

        // Fetch officers
        List<OfficerProfile> officers;
        
        if (departmentId != null) {
            // Get department officers for specific department
            officers = officerProfileRepository.findByDepartment_DepartmentIdAndActiveTrue(departmentId);
        } else if (wardId != null) {
            // Get ward officers for citizen's ward
            officers = officerProfileRepository.findByWard_WardIdAndUser_RoleAndActiveTrue(
                wardId, RoleName.WARD_OFFICER
            );
        } else {
            // Get all active officers
            officers = officerProfileRepository.findByActiveTrue();
        }

        // Map to DTOs
        return officers.stream()
            .map(this::mapOfficerToDTO)
            .collect(Collectors.toList());
    }

    private Map<String, Object> mapOfficerToDTO(OfficerProfile officer) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("userId", officer.getUser().getUserId());
        dto.put("name", officer.getUser().getName());
        dto.put("email", officer.getUser().getEmail());
        dto.put("mobile", officer.getUser().getMobile());
        dto.put("role", officer.getUser().getRole().name());
        dto.put("designation", officer.getDesignation());
        dto.put("employeeId", officer.getEmployeeId());
        
        if (officer.getWard() != null) {
            dto.put("wardId", officer.getWard().getWardId());
            dto.put("wardName", officer.getWard().getAreaName());
        }
        
        if (officer.getDepartment() != null) {
            dto.put("departmentId", officer.getDepartment().getDepartmentId());
            dto.put("departmentName", officer.getDepartment().getName());
        }
        
        dto.put("active", officer.isActive());
        
        return dto;
    }
}
