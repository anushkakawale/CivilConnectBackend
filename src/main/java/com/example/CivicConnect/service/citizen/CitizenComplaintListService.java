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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CitizenComplaintListService {

    private final ComplaintRepository complaintRepository;

    public Map<String, Object> getMyComplaints(
            User citizen,
            int page,
            int size,
            ComplaintStatus status,
            Priority priority,
            SLAStatus slaStatus
    ) {
        // Create pageable with sorting (newest first)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Fetch complaints
        Page<Complaint> complaintPage;
        
        if (slaStatus != null && status != null) {
            complaintPage = complaintRepository
                .findByCitizenAndStatusAndSla_Status(
                    citizen, status, slaStatus, pageable
                );

        } else if (slaStatus != null) {
            complaintPage = complaintRepository
                .findByCitizenAndSla_Status(
                    citizen, slaStatus, pageable
                );

        } else if (status != null && priority != null) {
            complaintPage = complaintRepository
                .findByCitizenAndStatusAndPriority(
                    citizen, status, priority, pageable
                );

        } else if (status != null) {
            complaintPage = complaintRepository
                .findByCitizenAndStatus(citizen, status, pageable);

        } else if (priority != null) {
            complaintPage = complaintRepository
                .findByCitizenAndPriority(citizen, priority, pageable);

        } else {
            complaintPage = complaintRepository
                .findByCitizen(citizen, pageable);
        }


        // Map to DTOs
        List<Map<String, Object>> complaints = complaintPage.getContent().stream()
            .map(this::mapComplaintToDTO)
            .collect(Collectors.toList());

        // Build response
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

        dto.put("wardName",
                complaint.getWard() != null
                        ? complaint.getWard().getAreaName()
                        : null);

        dto.put("departmentName",
                complaint.getDepartment() != null
                        ? complaint.getDepartment().getName()
                        : null);

        dto.put("createdAt", complaint.getCreatedAt());
        dto.put("updatedAt", complaint.getUpdatedAt());

        // 🖼 Images
        dto.put("imageCount",
                complaint.getImages() != null
                        ? complaint.getImages().size()
                        : 0);

        // ⏱ SLA BLOCK (SAFE + CORRECT)
        if (complaint.getSla() != null) {

            dto.put("slaStatus",
                    complaint.getSla().getStatus().name());

            dto.put("slaDeadline",
                    complaint.getSla().getSlaDeadline());

            long remainingMinutes =
                    java.time.Duration.between(
                            java.time.LocalDateTime.now(),
                            complaint.getSla().getSlaDeadline()
                    ).toMinutes();

            dto.put("slaRemainingMinutes",
                    Math.max(remainingMinutes, 0));

            dto.put("slaBreached",
                    complaint.getSla().getStatus() == SLAStatus.BREACHED);

        } else {
            dto.put("slaStatus", null);
            dto.put("slaDeadline", null);
            dto.put("slaRemainingMinutes", null);
            dto.put("slaBreached", false);
        }

        return dto;
    }
    private final ComplaintStatusHistoryRepository historyRepository;

    public List<ComplaintTimelineDTO> getTimeline(Long complaintId) {
        return historyRepository
                .findByComplaint_ComplaintIdOrderByChangedAtAsc(complaintId)
                .stream()
                .map(h -> new ComplaintTimelineDTO(
                        h.getStatus().name(),
                        h.getChangedBy().getRole().name(),
                        h.getChangedAt()
                ))
                .toList();
}}
