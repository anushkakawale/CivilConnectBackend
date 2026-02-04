package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.ComplaintSummaryDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentDashboardService1 {

    private final ComplaintRepository complaintRepository;
    private final com.example.CivicConnect.repository.OfficerProfileRepository officerProfileRepository;

    /* 
    public DepartmentDashboardService(
            ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }
    */
    // Replaced manual constructor with @RequiredArgsConstructor

    public List<Complaint> myWork(Long officerId) {
        return complaintRepository.findByAssignedOfficer_UserIdAndStatusIn(
                officerId,
                List.of(
                        ComplaintStatus.ASSIGNED,
                        ComplaintStatus.IN_PROGRESS
                )
        );
    }
    
    // ✅ NEW: Paginated version for controller
    public Page<ComplaintSummaryDTO> myWork(Long officerId, Pageable pageable) {
        
        // 🔒 SECURITY: Get Officer's Department
        var profile = officerProfileRepository.findByUser_UserId(officerId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        return complaintRepository
                .findByAssignedOfficer_UserIdAndDepartment_DepartmentIdAndStatusIn(
                        officerId,
                        profile.getDepartment().getDepartmentId(),
                        List.of(
                                ComplaintStatus.ASSIGNED,
                                ComplaintStatus.IN_PROGRESS
                        ),
                        pageable
                )
                .map(this::toSummaryDTO);
    }
    
    // ✅ Dashboard Summary
    public com.example.CivicConnect.dto.DashboardSummaryDTO getDashboardSummary(Long officerId) {
        long assignedToMe = complaintRepository.countByAssignedOfficer_UserIdAndStatusIn(
                officerId,
                List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS)
        );
        
        long inProgress = complaintRepository.countByAssignedOfficer_UserIdAndStatus(
                officerId, ComplaintStatus.IN_PROGRESS
        );
        
        long resolved = complaintRepository.countByAssignedOfficer_UserIdAndStatus(
                officerId, ComplaintStatus.RESOLVED
        );
        
        long slaBreached = complaintRepository.countByAssignedOfficer_UserIdAndSlaBreachedTrue(officerId);
        
        long totalAssigned = complaintRepository.countByAssignedOfficer_UserId(officerId);
        
        double slaCompliance = totalAssigned > 0 
                ? ((double)(totalAssigned - slaBreached) / totalAssigned * 100) 
                : 0;
        
        // Fetch department name for the summary
        var profile = officerProfileRepository.findByUser_UserId(officerId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        return com.example.CivicConnect.dto.DashboardSummaryDTO.builder()
                .assignedToMe(assignedToMe)
                .inProgress(inProgress)
                .resolved(resolved)
                .slaBreached(slaBreached)
                .slaCompliancePercent(slaCompliance)
                .totalComplaints(totalAssigned)
                .departmentName(profile.getDepartment().getName())
                .wardName(profile.getWard().getAreaName())
                .build();
    }
    
    private ComplaintSummaryDTO toSummaryDTO(Complaint complaint) {
        return new ComplaintSummaryDTO(
                complaint.getComplaintId(),
                complaint.getTitle(),
                complaint.getStatus(),
                complaint.getPriority(),
                complaint.getDepartment().getName(),
                complaint.getWard().getAreaName(),
                (complaint.getImages() != null && !complaint.getImages().isEmpty()) ? complaint.getImages().get(0).getImageUrl() : null,
                complaint.getCreatedAt()
        );
    }

    // ✅ NEW: Get colleagues in the same ward and department
    public List<com.example.CivicConnect.dto.OfficerDTO> getDepartmentColleagues(Long officerId) {
        var profile = officerProfileRepository.findByUser_UserId(officerId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        return officerProfileRepository.findByWard_WardIdAndDepartment_DepartmentId(
                profile.getWard().getWardId(), 
                profile.getDepartment().getDepartmentId()
               )
               .stream()
               .filter(p -> !p.getUser().getUserId().equals(officerId)) // Exclude self
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
}
