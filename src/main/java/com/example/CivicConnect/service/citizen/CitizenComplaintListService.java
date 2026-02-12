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

import com.example.CivicConnect.dto.ComplaintSummaryDTO;
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

    public Page<ComplaintSummaryDTO> getWardComplaints(
            User citizen,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        com.example.CivicConnect.entity.profiles.CitizenProfile profile = 
            citizenProfileRepository.findByUser_UserId(citizen.getUserId())
            .orElseThrow(() -> new RuntimeException("Citizen profile not found"));

        if (profile.getWard() == null) {
            return Page.empty(pageable);
        }

        return complaintRepository
                .findByWard_WardId(profile.getWard().getWardId(), pageable)
                .map(this::mapToSummaryDTO);
    }

    public Page<ComplaintSummaryDTO> getMyComplaints(
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

        return complaintPage.map(this::mapToSummaryDTO);
    }

    private ComplaintSummaryDTO mapToSummaryDTO(Complaint c) {
        return ComplaintSummaryDTO.builder()
                .complaintId(c.getComplaintId())
                .title(c.getTitle())
                .status(c.getStatus())
                .priority(c.getPriority())
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                .wardName(c.getWard() != null ? c.getWard().getAreaName() : null)
                .imageUrl((c.getImages() != null && !c.getImages().isEmpty()) 
                        ? "/uploads/" + c.getImages().get(0).getImageUrl() : null)
                .createdAt(c.getCreatedAt())
                .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : null)
                .slaDeadline(c.getSla() != null ? c.getSla().getSlaDeadline() : null)
                .rating(c.getRating())
                .feedback(c.getFeedback())
                .build();
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
