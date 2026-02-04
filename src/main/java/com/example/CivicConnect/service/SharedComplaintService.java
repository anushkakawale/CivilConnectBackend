package com.example.CivicConnect.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.CivicConnect.dto.ComplaintDetailDTO;
import com.example.CivicConnect.dto.StatusHistoryDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;

    public ComplaintDetailDTO getComplaintDetails(Long complaintId) {
        Complaint c = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + complaintId));

        List<StatusHistoryDTO> history = historyRepository.findByComplaintOrderByChangedAtDesc(c)
                .stream()
                .map(h -> new StatusHistoryDTO(
                        h.getStatus(),
                        h.getChangedAt(),
                        h.getChangedBy() != null ? h.getChangedBy().getName() : "System"
                ))
                .collect(Collectors.toList());

        List<String> imageUrls = c.getImages() != null ? 
                c.getImages().stream().map(img -> img.getImageUrl()).collect(Collectors.toList()) : 
                java.util.Collections.emptyList();

        return ComplaintDetailDTO.builder()
                .complaintId(c.getComplaintId())
                .title(c.getTitle())
                .description(c.getDescription())
                .location(c.getLocation())
                .category(c.getCategory())
                .status(c.getStatus())
                .priority(c.getPriority())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .citizenName(c.getCitizen() != null ? c.getCitizen().getName() : "N/A")
                .citizenMobile(c.getCitizen() != null ? c.getCitizen().getMobile() : "N/A")
                .assignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Unassigned")
                .assignedOfficerMobile(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getMobile() : "N/A")
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : "N/A")
                .wardName(c.getWard() != null ? c.getWard().getAreaName() : "N/A")
                .slaDeadline(c.getSlaDeadline())
                .slaBreached(c.isSlaBreached())
                .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : "N/A")
                .imageUrls(imageUrls)
                .history(history)
                .build();
    }
}
