package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;

@Service
public class WardOfficerDashboardService {

    private final ComplaintApprovalRepository approvalRepository;

    public WardOfficerDashboardService(
            ComplaintApprovalRepository approvalRepository) {
        this.approvalRepository = approvalRepository;
    }

    public List<ComplaintApproval> getPendingApprovals() {
        return approvalRepository.findByStatusAndRoleAtTime(
                ApprovalStatus.PENDING,
                RoleName.WARD_OFFICER
        );
    }
}
