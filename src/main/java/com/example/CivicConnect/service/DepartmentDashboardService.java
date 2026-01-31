package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.ComplaintSummaryDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;

@Service
public class DepartmentDashboardService {

    private final ComplaintRepository complaintRepository;

    public DepartmentDashboardService(
            ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

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
        return complaintRepository
                .findByAssignedOfficer_UserIdAndStatusIn(
                        officerId,
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
        
        return com.example.CivicConnect.dto.DashboardSummaryDTO.builder()
                .assignedToMe(assignedToMe)
                .inProgress(inProgress)
                .resolved(resolved)
                .slaBreached(slaBreached)
                .slaCompliancePercent(slaCompliance)
                .totalComplaints(totalAssigned)
                .build();
    }
    
    private ComplaintSummaryDTO toSummaryDTO(Complaint complaint) {
        return new ComplaintSummaryDTO(
                complaint.getComplaintId(),
                complaint.getTitle(),
                complaint.getStatus(),
                complaint.getCreatedAt()
        );
    }
}
