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
    
    private ComplaintSummaryDTO toSummaryDTO(Complaint complaint) {
        return new ComplaintSummaryDTO(
                complaint.getComplaintId(),
                complaint.getTitle(),
                complaint.getStatus(),
                complaint.getCreatedAt()
        );
    }
}
