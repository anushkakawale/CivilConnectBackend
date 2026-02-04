package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

@Service
public class WardOfficerDashboardService {

    private final ComplaintApprovalRepository approvalRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public WardOfficerDashboardService(
            ComplaintApprovalRepository approvalRepository,
            OfficerProfileRepository officerProfileRepository) {
        this.approvalRepository = approvalRepository;
        this.officerProfileRepository = officerProfileRepository;
    }

    public List<ComplaintApproval> getPendingApprovals(Long officerUserId) {
    	OfficerProfile profile = officerProfileRepository.findByUser_UserId(officerUserId)
    			.orElseThrow(() -> new RuntimeException("Officer profile not found"));
    	
        return approvalRepository.findByComplaint_Ward_WardIdAndStatusAndRoleAtTime(
                profile.getWard().getWardId(),
        		ApprovalStatus.PENDING,
                RoleName.WARD_OFFICER
        );
    }
}
