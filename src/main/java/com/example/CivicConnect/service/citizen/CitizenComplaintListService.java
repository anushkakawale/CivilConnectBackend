package com.example.CivicConnect.service.citizen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.ComplaintTimelineDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.Priority;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;

@Service
public class CitizenComplaintListService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final com.example.CivicConnect.repository.CitizenProfileRepository citizenProfileRepository;

    public CitizenComplaintListService(ComplaintRepository complaintRepository, 
                                     ComplaintStatusHistoryRepository historyRepository,
                                     com.example.CivicConnect.repository.CitizenProfileRepository citizenProfileRepository) {
        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.citizenProfileRepository = citizenProfileRepository;
    }

    public Map<String, Object> getWardComplaints(
            User citizen,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        com.example.CivicConnect.entity.profiles.CitizenProfile profile = 
            citizenProfileRepository.findByUser_UserId(citizen.getUserId())
            .orElseThrow(() -> new RuntimeException("Citizen profile not found"));

        if (profile.getWard() == null) {
            return Map.of("content", List.of(), "totalElements", 0);
        }

        Page<Complaint> complaintPage = complaintRepository
                .findByWard_WardId(profile.getWard().getWardId(), pageable);

        List<Map<String, Object>> complaints = complaintPage.getContent().stream()
                .map(this::mapComplaintToDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", complaints);
        response.put("totalElements", complaintPage.getTotalElements());
        response.put("totalPages", complaintPage.getTotalPages());
        return response;
    }

    public Map<String, Object> getMyComplaints(
            User citizen,
            int page,
            int size,
            ComplaintStatus status,
            Priority priority,
            SLAStatus slaStatus
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Complaint> complaintPage;
        
        if (slaStatus != null && status != null) {
            complaintPage = complaintRepository.findByCitizenAndStatusAndSla_Status(citizen, status, slaStatus, pageable);
        } else if (slaStatus != null) {
            complaintPage = complaintRepository.findByCitizenAndSla_Status(citizen, slaStatus, pageable);
        } else if (status != null && priority != null) {
            complaintPage = complaintRepository.findByCitizenAndStatusAndPriority(citizen, status, priority, pageable);
        } else if (status != null) {
            complaintPage = complaintRepository.findByCitizenAndStatus(citizen, status, pageable);
        } else if (priority != null) {
            complaintPage = complaintRepository.findByCitizenAndPriority(citizen, priority, pageable);
        } else {
            complaintPage = complaintRepository.findByCitizen(citizen, pageable);
        }

        List<Map<String, Object>> complaints = complaintPage.getContent().stream()
            .map(this::mapComplaintToDTO)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", complaints);
        response.put("totalElements", complaintPage.getTotalElements());
        response.put("totalPages", complaintPage.getTotalPages());
        response.put("currentPage", complaintPage.getNumber());
        response.put("pageSize", complaintPage.getSize());
        return response;
    }

    private Map<String, Object> mapComplaintToDTO(Complaint complaint) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("complaintId", complaint.getComplaintId());
        dto.put("title", complaint.getTitle());
        dto.put("description", complaint.getDescription());
        dto.put("category", complaint.getCategory());
        dto.put("priority", complaint.getPriority().name());
        dto.put("status", complaint.getStatus().name());
        dto.put("location", complaint.getLocation());
        dto.put("wardName", complaint.getWard() != null ? complaint.getWard().getAreaName() : null);
        dto.put("departmentName", complaint.getDepartment() != null ? complaint.getDepartment().getName() : null);
        dto.put("createdAt", complaint.getCreatedAt());
        dto.put("updatedAt", complaint.getUpdatedAt());
        dto.put("imageCount", complaint.getImages() != null ? complaint.getImages().size() : 0);

        if (complaint.getSla() != null) {
            dto.put("slaStatus", complaint.getSla().getStatus().name());
            dto.put("slaDeadline", complaint.getSla().getSlaDeadline());
            long remainingMinutes = java.time.Duration.between(java.time.LocalDateTime.now(), complaint.getSla().getSlaDeadline()).toMinutes();
            dto.put("slaRemainingMinutes", Math.max(remainingMinutes, 0));
            dto.put("slaBreached", complaint.getSla().getStatus() == SLAStatus.BREACHED);
        } else {
            dto.put("slaStatus", null);
            dto.put("slaDeadline", null);
            dto.put("slaRemainingMinutes", null);
            dto.put("slaBreached", false);
        }
        if (complaint.getImages() != null && !complaint.getImages().isEmpty()) {
            dto.put("imageUrl", "/uploads/" + complaint.getImages().get(0).getImageUrl());
        } else {
            dto.put("imageUrl", null);
        }

        dto.put("rating", complaint.getRating());
        dto.put("feedback", complaint.getFeedback());

        return dto;
    }

    public List<ComplaintTimelineDTO> getTimeline(Long complaintId) {
        return historyRepository.findByComplaint_ComplaintIdOrderByChangedAtAsc(complaintId)
                .stream()
                .map(h -> new ComplaintTimelineDTO(
                        h.getStatus().name(),
                        h.getChangedBy().getRole().name(),
                        h.getChangedAt()
                ))
                .toList();
    }
}
