package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

@Service
public class WardOfficerDashboardService {

    private final ComplaintApprovalRepository approvalRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final ComplaintRepository complaintRepository;

    public WardOfficerDashboardService(
            ComplaintApprovalRepository approvalRepository,
            OfficerProfileRepository officerProfileRepository,
            ComplaintRepository complaintRepository) {
        this.approvalRepository = approvalRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.complaintRepository = complaintRepository;
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

    public java.util.Map<String, Object> getWardStats(Long officerUserId) {
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(officerUserId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        Long wardId = profile.getWard().getWardId();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalComplaints", complaintRepository.countByWard_WardId(wardId));
        stats.put("pendingComplaints", complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.SUBMITTED));
        stats.put("inProgressComplaints", complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.IN_PROGRESS));
        stats.put("resolvedComplaints", complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.RESOLVED));
        
        return stats;
    }

    public List<com.example.CivicConnect.entity.complaint.Complaint> getAllWardComplaints(Long officerUserId) {
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(officerUserId)
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        return complaintRepository.findByWard_WardIdOrderByCreatedAtDesc(profile.getWard().getWardId());
    }
}
