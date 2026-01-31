package com.example.CivicConnect.service.citizen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.ComplaintRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CitizenAreaComplaintsService {

    private final ComplaintRepository complaintRepository;
    private final CitizenProfileRepository citizenProfileRepository;

    public List<Map<String, Object>> getAreaComplaints(User citizen, Long wardId) {
        // If wardId not provided, use citizen's ward
        if (wardId == null) {
            CitizenProfile profile = citizenProfileRepository
                .findByUser_UserId(citizen.getUserId())
                .orElseThrow(() -> new RuntimeException("Citizen profile not found"));
            
            wardId = profile.getWard() != null ? profile.getWard().getWardId() : null;
        }

        // Fetch complaints for the ward
        List<Complaint> complaints;
        if (wardId != null) {
            complaints = complaintRepository.findByWard_WardId(wardId);
        } else {
            complaints = List.of(); // Empty list if no ward
        }

        // Map to DTOs (only public info for map view)
        return complaints.stream()
            .filter(c -> c.getLatitude() != null && c.getLongitude() != null) // Only complaints with location
            .map(this::mapComplaintToMapDTO)
            .collect(Collectors.toList());
    }

    private Map<String, Object> mapComplaintToMapDTO(Complaint complaint) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("complaintId", complaint.getComplaintId());
        dto.put("title", complaint.getTitle());
        dto.put("category", complaint.getCategory()); // category is String, not enum
        dto.put("priority", complaint.getPriority().name());
        dto.put("status", complaint.getStatus().name());
        dto.put("latitude", complaint.getLatitude());
        dto.put("longitude", complaint.getLongitude());
        dto.put("createdAt", complaint.getCreatedAt());
        dto.put("location", complaint.getLocation());
        
        return dto;
    }
}
