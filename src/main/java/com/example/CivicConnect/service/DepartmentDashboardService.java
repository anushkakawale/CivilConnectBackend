package com.example.CivicConnect.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.ComplaintSummaryDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentDashboardService {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public Page<ComplaintSummaryDTO> getAssignedComplaints(Long officerId, Pageable pageable) {
        return getAssignedComplaints(officerId, List.of(
                ComplaintStatus.ASSIGNED, 
                ComplaintStatus.IN_PROGRESS, 
                ComplaintStatus.ON_HOLD, 
                ComplaintStatus.ESCALATED,
                ComplaintStatus.REOPENED
        ), pageable);
    }

    public Page<ComplaintSummaryDTO> getAssignedComplaints(Long officerId, List<ComplaintStatus> statuses, Pageable pageable) {
        var profile = officerProfileRepository.findByUser_UserId(officerId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        return complaintRepository.findByAssignedOfficer_UserIdAndDepartment_DepartmentIdAndStatusIn(
                officerId,
                profile.getDepartment().getDepartmentId(),
                statuses,
                pageable
        ).map(this::toSummaryDTO);
    }
    
    public Map<String, Object> getOfficerSummary(Long officerId) {
        var profile = officerProfileRepository.findByUser_UserId(officerId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        long inProgress = complaintRepository.countByAssignedOfficer_UserIdAndStatus(
                officerId, ComplaintStatus.IN_PROGRESS
        );
        long resolved = complaintRepository.countByAssignedOfficer_UserIdAndStatus(
                officerId, ComplaintStatus.RESOLVED
        );
        long totalAssigned = complaintRepository.countByAssignedOfficer_UserId(officerId);
        long slaBreached = complaintRepository.countByAssignedOfficer_UserIdAndSlaBreachedTrue(officerId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("officerName", profile.getUser().getName());
        summary.put("department", profile.getDepartment().getName());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAssigned", totalAssigned);
        stats.put("inProgress", inProgress);
        stats.put("pending", totalAssigned - inProgress - resolved);
        stats.put("resolved", resolved);
        
        Map<String, Object> sla = new HashMap<>();
        sla.put("breached", slaBreached);
        
        summary.put("statistics", stats);
        summary.put("sla", sla);

        return summary;
    }

    public List<ComplaintSummaryDTO> getPeerComplaints(Long officerId) {
        var profile = officerProfileRepository.findByUser_UserId(officerId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        if (profile.getWard() == null || profile.getDepartment() == null) {
            return List.of();
        }

        return complaintRepository.findByWard_WardIdAndDepartment_DepartmentId(
                profile.getWard().getWardId(),
                profile.getDepartment().getDepartmentId()
        ).stream()
        .filter(c -> c.getAssignedOfficer() == null || !c.getAssignedOfficer().getUserId().equals(officerId))
        .map(this::toSummaryDTO)
        .toList();
    }

    public List<com.example.CivicConnect.dto.OfficerDTO> getDepartmentColleagues(Long officerId) {
        var profile = officerProfileRepository.findByUser_UserId(officerId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        return officerProfileRepository.findByWard_WardIdAndDepartment_DepartmentId(
                profile.getWard().getWardId(),
                profile.getDepartment().getDepartmentId()
        ).stream()
                .filter(p -> !p.getUser().getUserId().equals(officerId))
                .map(p -> new com.example.CivicConnect.dto.OfficerDTO(
                        p.getUser().getUserId(),
                        p.getUser().getName(),
                        p.getUser().getEmail(),
                        p.getUser().getMobile(),
                        p.getDepartment().getName(),
                        p.getWard().getAreaName()
                ))
                .toList();
    }

    private ComplaintSummaryDTO toSummaryDTO(Complaint complaint) {
        String imageUrl = null;
        if (complaint.getImages() != null && !complaint.getImages().isEmpty()) {
            String rawUrl = complaint.getImages().get(0).getImageUrl();
            imageUrl = rawUrl != null ? "/uploads/" + rawUrl : null;
        }

        return ComplaintSummaryDTO.builder()
                .complaintId(complaint.getComplaintId())
                .title(complaint.getTitle())
                .status(complaint.getStatus())
                .priority(complaint.getPriority())
                .departmentName(complaint.getDepartment() != null ? complaint.getDepartment().getName() : "N/A")
                .wardName(complaint.getWard() != null ? complaint.getWard().getAreaName() : "N/A")
                .imageUrl(imageUrl)
                .createdAt(complaint.getCreatedAt())
                .slaStatus((complaint.getSla() != null && complaint.getSla().getStatus() != null) 
                    ? complaint.getSla().getStatus().name() : "ON_TRACK")
                .slaDeadline(complaint.getSla() != null ? complaint.getSla().getSlaDeadline() : null)
                .rating(complaint.getRating())
                .feedback(complaint.getFeedback())
                .build();
    }
}