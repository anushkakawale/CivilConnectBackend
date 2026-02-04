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
        var profile = officerProfileRepository.findByUser_UserId(officerId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        return complaintRepository.findByAssignedOfficer_UserIdAndDepartment_DepartmentIdAndStatusIn(
                officerId,
                profile.getDepartment().getDepartmentId(),
                List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS),
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
    
    private ComplaintSummaryDTO toSummaryDTO(Complaint complaint) {
        return new ComplaintSummaryDTO(
                complaint.getComplaintId(),
                complaint.getTitle(),
                complaint.getStatus(),
                complaint.getPriority(),
                complaint.getDepartment().getName(),
                complaint.getWard().getAreaName(),
                (complaint.getImages() != null && !complaint.getImages().isEmpty()) 
                    ? complaint.getImages().get(0).getImageUrl() : null,
                complaint.getCreatedAt()
        );
    }
}