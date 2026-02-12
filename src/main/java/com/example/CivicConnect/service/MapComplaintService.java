package com.example.CivicConnect.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.ComplaintMapDTO;
import com.example.CivicConnect.dto.OfficerDirectoryDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;

    /**
     * Get complaints for map view based on user role
     * - CITIZEN: All complaints in their ward
     * - DEPARTMENT_OFFICER: Only their assigned complaints
     * - WARD_OFFICER: All complaints in their ward
     * - ADMIN: All complaints system-wide
     */
    public List<ComplaintMapDTO> getMapComplaints(User user, ComplaintStatus status) {
        
        List<Complaint> complaints;
        
        switch (user.getRole()) {
            case CITIZEN -> {
                // Citizen sees all complaints in their ward
                Long wardId = citizenProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Citizen profile not found"))
                        .getWard()
                        .getWardId();
                
                complaints = status != null 
                    ? complaintRepository.findByWard_WardIdAndStatus(wardId, status)
                    : complaintRepository.findByWard_WardId(wardId);
            }
            
            case DEPARTMENT_OFFICER -> {
                // Department Officer sees only their assigned complaints
                complaints = status != null
                    ? complaintRepository.findByAssignedOfficer_UserIdAndStatus(user.getUserId(), status)
                    : complaintRepository.findByAssignedOfficer_UserId(user.getUserId());
            }
            
            case WARD_OFFICER -> {
                // Ward Officer sees all complaints in their ward
                Long wardId = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Ward Officer profile not found"))
                        .getWard()
                        .getWardId();
                
                complaints = status != null
                    ? complaintRepository.findByWard_WardIdAndStatus(wardId, status)
                    : complaintRepository.findByWard_WardId(wardId);
            }
            
            case ADMIN -> {
                // Admin sees all complaints
                complaints = status != null
                    ? complaintRepository.findByStatus(status)
                    : complaintRepository.findAll();
            }
            
            default -> throw new RuntimeException("Invalid user role");
        }
        
        return complaints.stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Get complaints for map with all statuses grouped
     */
    public Map<String, Object> getMapComplaintsGrouped(User user) {
        List<Complaint> complaints = getComplaintsForUser(user);
        
        Map<String, List<ComplaintMapDTO>> groupedByStatus = complaints.stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .collect(Collectors.groupingBy(
                    c -> c.getStatus().name(),
                    Collectors.mapping(this::mapToDTO, Collectors.toList())
                ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("complaintsByStatus", groupedByStatus);
        response.put("totalComplaints", complaints.size());
        response.put("statusCounts", getStatusCounts(complaints));
        
        return response;
    }

    /**
     * Get officer directory based on user role
     * - CITIZEN: All department officers in their ward
     * - DEPARTMENT_OFFICER: All peer department officers in same department
     * - WARD_OFFICER: All department officers in their ward
     * - ADMIN: All officers system-wide
     */
    public List<OfficerDirectoryDTO> getOfficerDirectory(User user) {
        List<OfficerProfile> officers;
        
        switch (user.getRole()) {
            case CITIZEN -> {
                // Citizen sees all department officers in their ward
                Long wardId = citizenProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Citizen profile not found"))
                        .getWard()
                        .getWardId();
                
                officers = officerProfileRepository.findByWard_WardId(wardId).stream()
                        .filter(o -> o.getUser().getRole() == RoleName.DEPARTMENT_OFFICER)
                        .toList();
            }
            
            case DEPARTMENT_OFFICER -> {
                // Department Officer sees peer officers in same department
                OfficerProfile myProfile = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Officer profile not found"));
                
                Long deptId = myProfile.getDepartment().getDepartmentId();
                
                officers = officerProfileRepository.findByDepartment_DepartmentId(deptId).stream()
                        .filter(o -> !o.getUser().getUserId().equals(user.getUserId())) // Exclude self
                        .toList();
            }
            
            case WARD_OFFICER -> {
                // Ward Officer sees all department officers in their ward
                Long wardId = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Ward Officer profile not found"))
                        .getWard()
                        .getWardId();
                
                officers = officerProfileRepository.findByWard_WardId(wardId).stream()
                        .filter(o -> o.getUser().getRole() == RoleName.DEPARTMENT_OFFICER)
                        .toList();
            }
            
            case ADMIN -> {
                // Admin sees all officers
                officers = officerProfileRepository.findAll();
            }
            
            default -> throw new RuntimeException("Invalid user role");
        }
        
        return officers.stream()
                .map(this::mapToOfficerDTO)
                .toList();
    }

    // Helper methods
    
    private List<Complaint> getComplaintsForUser(User user) {
        return switch (user.getRole()) {
            case CITIZEN -> {
                Long wardId = citizenProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow()
                        .getWard()
                        .getWardId();
                yield complaintRepository.findByWard_WardId(wardId);
            }
            case DEPARTMENT_OFFICER -> 
                complaintRepository.findByAssignedOfficer_UserId(user.getUserId());
            case WARD_OFFICER -> {
                Long wardId = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow()
                        .getWard()
                        .getWardId();
                yield complaintRepository.findByWard_WardId(wardId);
            }
            case ADMIN -> complaintRepository.findAll();
            default -> throw new RuntimeException("Invalid role");
        };
    }
    
    private ComplaintMapDTO mapToDTO(Complaint c) {
        ComplaintMapDTO dto = new ComplaintMapDTO();
        dto.setComplaintId(c.getComplaintId());
        dto.setLatitude(c.getLatitude() != null ? c.getLatitude() : 0.0);
        dto.setLongitude(c.getLongitude() != null ? c.getLongitude() : 0.0);
        dto.setStatus(c.getStatus());
        dto.setSlaStatus(c.getSla() != null ? c.getSla().getStatus() : null);
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setDepartmentName(c.getDepartment() != null ? c.getDepartment().getName() : "N/A");
        dto.setWardName(c.getWard() != null ? c.getWard().getAreaName() : "N/A");
        dto.setPriority(c.getPriority() != null ? c.getPriority().name() : "MEDIUM");
        dto.setCreatedAt(c.getCreatedAt());
        
        if (c.getImages() != null && !c.getImages().isEmpty()) {
            dto.setImageUrl(c.getImages().get(0).getImageUrl());
        }
        
        return dto;
    }
    
    private OfficerDirectoryDTO mapToOfficerDTO(OfficerProfile profile) {
        OfficerDirectoryDTO dto = new OfficerDirectoryDTO();
        dto.setUserId(profile.getUser().getUserId());
        dto.setName(profile.getUser().getName());
        dto.setEmail(profile.getUser().getEmail());
        dto.setMobile(profile.getUser().getMobile());
        dto.setRole(profile.getUser().getRole().name());
        dto.setDepartment(profile.getDepartment() != null ? profile.getDepartment().getName() : "N/A");
        dto.setWardNumber(profile.getWard() != null ? profile.getWard().getWardNumber() : "N/A");
        dto.setSpecialization(profile.getDesignation()); // Mapping designation to specialization in DTO
        
        // Count active complaints
        long activeComplaints = complaintRepository
                .countByAssignedOfficer_UserIdAndStatusIn(
                    profile.getUser().getUserId(),
                    List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS)
                );
        dto.setActiveComplaintsCount((int) activeComplaints);
        
        return dto;
    }
    
    private Map<String, Long> getStatusCounts(List<Complaint> complaints) {
        return complaints.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getStatus().name(),
                    Collectors.counting()
                ));
    }
}