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
    private final com.example.CivicConnect.repository.WardRepository wardRepository;

    /**
     * Get complaints for map view based on user role and filters
     */
    public List<ComplaintMapDTO> getMapComplaints(
            User user, 
            ComplaintStatus status, 
            Long departmentId, 
            Long wardId,
            boolean myComplaintsOnly) {
        
        List<Complaint> complaints;
        
        if (myComplaintsOnly) {
            complaints = status != null 
                ? complaintRepository.findByCitizen_UserIdAndStatus(user.getUserId(), status, org.springframework.data.domain.Pageable.unpaged()).getContent()
                : complaintRepository.findByCitizen_UserIdOrderByCreatedAtDesc(user.getUserId());
        } else {
            switch (user.getRole()) {
                case CITIZEN -> {
                    Long myWardId = citizenProfileRepository
                            .findByUser_UserId(user.getUserId())
                            .orElseThrow(() -> new RuntimeException("Citizen profile not found"))
                            .getWard()
                            .getWardId();
                    
                    complaints = complaintRepository.filterForMap(myWardId, departmentId, status, LocalDateTime.now().minusYears(1));
                }
                
                case DEPARTMENT_OFFICER -> {
                    complaints = status != null
                        ? complaintRepository.findByAssignedOfficer_UserIdAndStatus(user.getUserId(), status)
                        : complaintRepository.findByAssignedOfficer_UserId(user.getUserId());
                }
                
                case WARD_OFFICER -> {
                    Long myWardId = officerProfileRepository
                            .findByUser_UserId(user.getUserId())
                            .orElseThrow(() -> new RuntimeException("Ward Officer profile not found"))
                            .getWard()
                            .getWardId();
                    
                    complaints = complaintRepository.filterForMap(myWardId, departmentId, status, LocalDateTime.now().minusYears(1));
                }
                
                case ADMIN -> {
                    complaints = complaintRepository.filterForMap(wardId, departmentId, status, LocalDateTime.now().minusYears(1));
                }
                
                default -> throw new RuntimeException("Invalid user role");
            }
        }
        
        return complaints.stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Get complaints for map view based on user role
     */
    public List<ComplaintMapDTO> getMapComplaints(User user, ComplaintStatus status) {
        return getMapComplaints(user, status, null, null, false);
    }

    /**
     * Get Ward boundaries with statistics for Admin Map
     */
    public List<com.example.CivicConnect.dto.WardMapDTO> getWardBoundaries() {
        return wardRepository.findAll().stream().map(ward -> {
            long total = complaintRepository.countByWard_WardId(ward.getWardId());
            long resolved = complaintRepository.countByWard_WardIdAndStatus(ward.getWardId(), ComplaintStatus.CLOSED);
            long breached = complaintRepository.countByWard_WardIdAndSlaBreachedTrue(ward.getWardId());
            
            return com.example.CivicConnect.dto.WardMapDTO.builder()
                .wardId(ward.getWardId())
                .wardNumber(ward.getWardNumber())
                .areaName(ward.getAreaName())
                .boundaryCoords(ward.getBoundaryCoords())
                .totalComplaints(total)
                .resolvedPercentage(total > 0 ? (double) resolved / total * 100 : 0.0)
                .pendingComplaints(total - resolved)
                .slaBreachedCount(breached)
                .build();
        }).toList();
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
     * Get all citizen locations for Admin Map
     */
    public List<com.example.CivicConnect.dto.CitizenMapDTO> getCitizenLocations() {
        return citizenProfileRepository.findAll().stream()
            .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
            .map(p -> com.example.CivicConnect.dto.CitizenMapDTO.builder()
                .citizenId(p.getUser().getUserId())
                .name(p.getUser().getName())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .wardName(p.getWard() != null ? p.getWard().getAreaName() : "N/A")
                .complaintCount(complaintRepository.countByCitizen_UserId(p.getUser().getUserId()))
                .build())
            .toList();
    }

    /**
     * Get officer directory based on user role
     */
    public List<OfficerDirectoryDTO> getOfficerDirectory(User user) {
        List<OfficerProfile> officers;
        
        switch (user.getRole()) {
            case CITIZEN -> {
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
                OfficerProfile myProfile = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Officer profile not found"));
                
                Long deptId = myProfile.getDepartment().getDepartmentId();
                
                officers = officerProfileRepository.findByDepartment_DepartmentId(deptId).stream()
                        .filter(o -> !o.getUser().getUserId().equals(user.getUserId())) // Exclude self
                        .toList();
            }
            
            case WARD_OFFICER -> {
                Long wardId = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Ward Officer profile not found"))
                        .getWard()
                        .getWardId();
                
                officers = officerProfileRepository.findByWard_WardId(wardId).stream()
                        .filter(o -> o.getUser().getRole() == RoleName.DEPARTMENT_OFFICER)
                        .toList();
            }
            
            case ADMIN -> officers = officerProfileRepository.findAll();
            
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
        dto.setSlaBreached(c.isSlaBreached());
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
        dto.setSpecialization(profile.getDesignation());
        dto.setLatitude(profile.getLatitude());
        dto.setLongitude(profile.getLongitude());
        
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