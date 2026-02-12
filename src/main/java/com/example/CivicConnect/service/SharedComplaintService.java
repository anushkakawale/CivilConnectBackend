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
import com.example.CivicConnect.repository.AccessLogRepository;
import com.example.CivicConnect.dto.AuditLogDTO;

import com.example.CivicConnect.dto.FeedbackDisplayDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final AccessLogRepository accessLogRepository;

    public ComplaintDetailDTO getComplaintDetails(Long complaintId) {
        Complaint c = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + complaintId));

        List<StatusHistoryDTO> history = historyRepository.findByComplaintOrderByChangedAtDesc(c)
                .stream()
                .map(h -> new StatusHistoryDTO(
                        h.getStatus(),
                        h.getChangedAt(),
                        h.getChangedBy() != null ? h.getChangedBy().getName() : "System",
                        h.getRemarks()
                ))
                .collect(Collectors.toList());

        // ✅ NEW: Map images with full attribution
        List<com.example.CivicConnect.dto.ComplaintImageDTO> images = c.getImages() != null ? 
                c.getImages().stream().map(img -> 
                    com.example.CivicConnect.dto.ComplaintImageDTO.builder()
                        .imageId(img.getImageId())
                        .imageUrl("/uploads/" + img.getImageUrl())  // ✅ FIX: Ensure URL is constructed
                        .stage(img.getImageStage())
                        .uploadedBy(img.getUploadedBy() != null ? img.getUploadedBy().getName() : "Unknown")
                        .uploadedByRole(img.getUploadedBy() != null ? img.getUploadedBy().getRole().name() : "SYSTEM")
                        .uploadedAt(img.getUploadedAt())
                        .build()
                ).collect(Collectors.toList()) : 
                java.util.Collections.emptyList();

        // Legacy support: Simple URL list (deprecated)
        List<String> imageUrls = images.stream()
                .map(com.example.CivicConnect.dto.ComplaintImageDTO::getImageUrl)
                .collect(Collectors.toList());

        List<AuditLogDTO> auditLogs = accessLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("COMPLAINT", complaintId)
                .stream()
                .map(log -> new AuditLogDTO(
                    log.getLogId(),
                    log.getUser() != null ? log.getUser().getUserId() : null,
                    log.getUser() != null ? log.getUser().getName() : null,
                    log.getAction(),
                    log.getEntityType(),
                    log.getEntityId(),
                    log.getIpAddress(),
                    log.getCreatedAt()
                ))
                .collect(Collectors.toList());

        List<FeedbackDisplayDTO> feedbacks = c.getFeedbacks() != null ?
                c.getFeedbacks().stream().map(f -> FeedbackDisplayDTO.builder()
                        .userName(f.getCitizen() != null ? f.getCitizen().getName() : "Anonymous")
                        .rating(f.getRating())
                        .comment(f.getComment())
                        .createdAt(f.getCreatedAt())
                        .build()
                ).collect(Collectors.toList()) :
                java.util.Collections.emptyList();

        return ComplaintDetailDTO.builder()
                .complaintId(c.getComplaintId())
                .title(c.getTitle())
                .description(c.getDescription())
                .location(formatLocation(c))
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
                .assignedOfficerEmail(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getEmail() : "N/A")
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : "N/A")
                .departmentId(c.getDepartment() != null ? c.getDepartment().getDepartmentId() : null)
                .wardName(c.getWard() != null ? c.getWard().getAreaName() : "N/A")
                .wardId(c.getWard() != null ? c.getWard().getWardId() : null)
                .wardNumber(c.getWard() != null ? c.getWard().getWardNumber() : "N/A")
                .slaDeadline(c.getSlaDeadline())
                .slaBreached(c.isSlaBreached())
                .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : "N/A")
                .closedAt(c.getClosedAt())
                .rating(c.getRating())
                .feedback(c.getFeedback())
                .averageRating(c.getAverageRating())
                .totalRatings(c.getTotalRatings())
                .feedbacks(feedbacks)
                .canReopen(c.getStatus() == com.example.CivicConnect.entity.enums.ComplaintStatus.CLOSED && 
                           c.getUpdatedAt() != null && 
                           c.getUpdatedAt().isAfter(java.time.LocalDateTime.now().minusDays(7)))
                .images(images)
                .imageUrls(imageUrls)
                .history(history)
                .auditLogs(auditLogs)
                .slaDetails(buildSlaDetails(c)) // ✅ NEW: Populate comprehensive SLA details
                .build();
    }

    private com.example.CivicConnect.dto.SlaDetailDTO buildSlaDetails(Complaint c) {
        if (c.getSla() == null) {
            // Fallback if no specific SLA record exists but basic fields are present
            if (c.getSlaDeadline() != null) {
                return com.example.CivicConnect.dto.SlaDetailDTO.builder()
                        .deadline(c.getSlaDeadline())
                        .breached(c.isSlaBreached())
                        .status(c.isSlaBreached() ? com.example.CivicConnect.entity.enums.SLAStatus.BREACHED : com.example.CivicConnect.entity.enums.SLAStatus.ON_TRACK)
                        .priority(c.getPriority() != null ? c.getPriority().name() : "MEDIUM")
                        .build();
            }
            return null;
        }

        com.example.CivicConnect.entity.sla.ComplaintSla sla = c.getSla();
        
        // Calculate durations
        Long totalHoursAllocated = null;
        if (sla.getSlaStartTime() != null && sla.getSlaDeadline() != null) {
            totalHoursAllocated = java.time.Duration.between(sla.getSlaStartTime(), sla.getSlaDeadline()).toHours();
        }

        Long remainingHours = null;
        if (sla.getStatus() == com.example.CivicConnect.entity.enums.SLAStatus.ON_TRACK || 
            sla.getStatus() == com.example.CivicConnect.entity.enums.SLAStatus.WARNING) {
            if (sla.getSlaDeadline() != null) {
                java.time.Duration diff = java.time.Duration.between(java.time.LocalDateTime.now(), sla.getSlaDeadline());
                remainingHours = diff.toHours();
            }
        }

        return com.example.CivicConnect.dto.SlaDetailDTO.builder()
                .startTime(sla.getSlaStartTime())
                .deadline(sla.getSlaDeadline())
                .completionTime(sla.getSlaEndTime())
                .remainingHours(remainingHours)
                .totalHoursAllocated(totalHoursAllocated)
                .breached(sla.isSlaBreached())
                .status(sla.getStatus())
                .escalated(sla.isEscalated())
                .priority(c.getPriority() != null ? c.getPriority().name() : "MEDIUM")
                .build();
    }

    private String formatLocation(Complaint c) {
        if (c.getLocation() != null && !c.getLocation().trim().isEmpty() && !c.getLocation().equalsIgnoreCase("Unknown")) {
            return c.getLocation();
        }
        if (c.getLatitude() != null && c.getLongitude() != null) {
            return String.format("Lat: %.6f, Long: %.6f", c.getLatitude(), c.getLongitude());
        }
        if (c.getWard() != null) {
            return c.getWard().getAreaName() + " (Precise location unavailable)";
        }
        return "Location Unknown";
    }
}
