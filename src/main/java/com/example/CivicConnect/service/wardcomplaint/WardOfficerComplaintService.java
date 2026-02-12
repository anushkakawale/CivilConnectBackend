package com.example.CivicConnect.service.wardcomplaint;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.ComplaintClosureDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.service.NotificationService;

@Service
@Transactional
public class WardOfficerComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintApprovalRepository approvalRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final ComplaintSlaRepository slaRepository;

    public WardOfficerComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintApprovalRepository approvalRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationService notificationService,
            UserRepository userRepository,
            OfficerProfileRepository officerProfileRepository,
            ComplaintSlaRepository slaRepository) {

        this.complaintRepository = complaintRepository;
        this.approvalRepository = approvalRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.slaRepository = slaRepository;
    }
    
    public void approve(Long complaintId, User wardOfficer, String remarks) {
        Complaint complaint = getComplaint(complaintId);
        
        var profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        if (!complaint.getWard().getWardId().equals(profile.getWard().getWardId())) {
            throw new RuntimeException("ACCESS DENIED: You cannot act on complaints from other wards");
        }
        
        ComplaintApproval approval = getPendingApproval(complaint);

        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setDecidedBy(wardOfficer);
        approval.setRemarks(remarks);
        approval.setDecidedAt(LocalDateTime.now());

        complaint.setStatus(ComplaintStatus.APPROVED);
        complaint.setApprovedBy(wardOfficer);
        complaint.setApprovedAt(LocalDateTime.now());

        approvalRepository.save(approval);
        complaintRepository.save(complaint);

        logStatus(complaint, ComplaintStatus.APPROVED, wardOfficer, remarks);

        // Notify Admins
        notificationService.notifyAdmins(
            "Complaint Approved",
            "Complaint ID #" + complaint.getComplaintId() + " has been approved by Ward Officer " + wardOfficer.getName() + " and is ready for final closure.",
            complaint.getComplaintId(),
            NotificationType.STATUS_UPDATE
        );
    }

    public void reject(Long complaintId, User wardOfficer, String remarks) {
        Complaint complaint = getComplaint(complaintId);
        
        var profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        if (!complaint.getWard().getWardId().equals(profile.getWard().getWardId())) {
            throw new RuntimeException("ACCESS DENIED: You cannot act on complaints from other wards");
        }

        ComplaintApproval approval = getPendingApproval(complaint);

        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setDecidedBy(wardOfficer);
        approval.setRemarks(remarks);
        approval.setDecidedAt(LocalDateTime.now());

        complaint.setStatus(ComplaintStatus.ASSIGNED);

        approvalRepository.save(approval);
        complaintRepository.save(complaint);

        logStatus(complaint, ComplaintStatus.ASSIGNED, wardOfficer, "Rejected by Ward Officer: " + remarks);

        if (complaint.getAssignedOfficer() != null) {
            notificationService.notifyOfficer(
                complaint.getAssignedOfficer(),
                "Complaint Rejected",
                "Work on Complaint #" + complaint.getComplaintId() + " was rejected by Ward Officer. Reason: " + remarks,
                complaint.getComplaintId(),
                NotificationType.STATUS_UPDATE
            );
        }

        slaRepository.findByComplaint(complaint).ifPresent(sla -> {
            sla.setSlaEndTime(null); 
            if (sla.getSlaDeadline().isBefore(LocalDateTime.now())) {
                sla.setStatus(SLAStatus.BREACHED);
            } else {
                sla.setStatus(SLAStatus.ON_TRACK);
            }
            slaRepository.save(sla);
        });
    }

    private ComplaintApproval getPendingApproval(Complaint complaint) {
        return approvalRepository
                .findByComplaintAndStatus(complaint, ApprovalStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No pending approval record found for this complaint"));
    }

    public void assign(Long complaintId, Long officerId, User wardOfficer) {
        Complaint complaint = getComplaint(complaintId);

        var profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        if (!complaint.getWard().getWardId().equals(profile.getWard().getWardId())) {
            throw new RuntimeException("ACCESS DENIED: You cannot act on complaints from other wards");
        }

        User newOfficer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Target officer not found"));

        if (!newOfficer.getRole().name().contains("OFFICER")) {
            throw new RuntimeException("Can only assign to valid Officers");
        }

        complaint.setAssignedOfficer(newOfficer);
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        complaintRepository.save(complaint);

        logStatus(complaint, ComplaintStatus.ASSIGNED, wardOfficer, "Manual re-assignment to " + newOfficer.getName());

        notificationService.notifyComplaintAssigned(complaint, newOfficer);
    }

    private Complaint getComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + id));
    }

    public Page<Complaint> getWardComplaints(Long wardId, Pageable pageable) {
        return complaintRepository.findByWard_WardId(wardId, pageable);
    }

    public Page<Complaint> getUnassignedComplaints(Long wardId, Pageable pageable) {
        return complaintRepository.findByWard_WardIdAndAssignedOfficerIsNull(wardId, pageable);
    }

    public Page<Complaint> getComplaintsForApproval(Long wardId, Pageable pageable) {
        return complaintRepository.findByWard_WardIdAndStatus(wardId, ComplaintStatus.RESOLVED, pageable);
    }

    public Page<ComplaintClosureDTO> getClosedHistoryForWard(Long wardId, Pageable pageable) {
        Page<Complaint> closed = complaintRepository.findByWard_WardIdAndStatus(wardId, ComplaintStatus.CLOSED, pageable);
        return closed.map(this::mapToClosureDto);
    }

    // 🆕 NEW: Detailed tracking for closed complaints (like approval queue)
    public Page<com.example.CivicConnect.dto.ClosedComplaintTrackingDTO> getClosedComplaintsTracking(Long wardId, Pageable pageable) {
        Page<Complaint> closed = complaintRepository.findByWard_WardIdAndStatus(wardId, ComplaintStatus.CLOSED, pageable);
        return closed.map(this::mapToClosedTracking);
    }

    private com.example.CivicConnect.dto.ClosedComplaintTrackingDTO mapToClosedTracking(Complaint c) {
        String approvalRemarks = approvalRepository
                .findByComplaintAndStatus(c, ApprovalStatus.APPROVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintApproval::getRemarks)
                .orElse("No remarks");

        String closureRemarks = historyRepository
                .findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.CLOSED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getRemarks)
                .orElse("N/A");

        // Get resolved timestamp
        LocalDateTime resolvedAt = historyRepository
                .findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.RESOLVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getChangedAt)
                .orElse(null);

        // Count images by stage
        long beforeCount = c.getImages() != null ? 
                c.getImages().stream()
                    .filter(img -> img.getImageStage() == com.example.CivicConnect.entity.enums.ImageStage.BEFORE_WORK)
                    .count() : 0;

        long afterCount = c.getImages() != null ? 
                c.getImages().stream()
                    .filter(img -> img.getImageStage() == com.example.CivicConnect.entity.enums.ImageStage.AFTER_RESOLUTION)
                    .count() : 0;

        return com.example.CivicConnect.dto.ClosedComplaintTrackingDTO.builder()
                .id(c.getComplaintId())
                .title(c.getTitle())
                .description(c.getDescription())
                .departmentName(c.getDepartment().getName())
                .wardName(c.getWard().getAreaName())
                .priority(c.getPriority().name())
                .citizenName(c.getCitizen().getName())
                .assignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Unassigned")
                .approvedByName(c.getApprovedBy() != null ? c.getApprovedBy().getName() : "N/A")
                .closedByAdminName(c.getClosedByAdmin() != null ? c.getClosedByAdmin().getName() : "N/A")
                .status(c.getStatus().name())
                .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : "N/A")
                .slaBreached(c.isSlaBreached())
                .createdAt(c.getCreatedAt())
                .resolvedAt(resolvedAt)
                .approvedAt(c.getApprovedAt())
                .closedAt(c.getClosedAt())
                .approvalRemarks(approvalRemarks)
                .closureRemarks(closureRemarks)
                .averageRating(c.getAverageRating())
                .totalRatings(c.getTotalRatings())
                .beforeImageCount((int) beforeCount)
                .afterImageCount((int) afterCount)
                .build();
    }

    private ComplaintClosureDTO mapToClosureDto(Complaint c) {
        String approvalRemarks = approvalRepository
                .findByComplaintAndStatus(c, ApprovalStatus.APPROVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintApproval::getRemarks)
                .orElse("No remarks");

        String closureRemarks = historyRepository
                .findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.CLOSED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getRemarks)
                .orElse("N/A");

        return ComplaintClosureDTO.builder()
                .id(c.getComplaintId())
                .title(c.getTitle())
                .wardName(c.getWard().getAreaName())
                .departmentName(c.getDepartment().getName())
                .priority(c.getPriority().name())
                .assignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Unassigned")
                .approvedByOfficerName(c.getApprovedBy() != null ? c.getApprovedBy().getName() : "N/A")
                .closedByAdminName(c.getClosedByAdmin() != null ? c.getClosedByAdmin().getName() : "N/A")
                .status(c.getStatus().name())
                .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : "N/A")
                .createdAt(c.getCreatedAt())
                .approvedAt(c.getApprovedAt())
                .closedAt(c.getClosedAt())
                .approvalRemarks(approvalRemarks)
                .closureRemarks(closureRemarks)
                .build();
    }

    public java.util.Map<String, Long> getWardStats(Long wardId) {
        return java.util.Map.of(
            "unassigned", complaintRepository.countByWard_WardIdAndAssignedOfficerIsNull(wardId),
            "pendingApproval", complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.RESOLVED),
            "totalOpen", complaintRepository.countByWard_WardIdAndStatusIn(wardId, java.util.List.of(
                ComplaintStatus.SUBMITTED, 
                ComplaintStatus.ASSIGNED, 
                ComplaintStatus.IN_PROGRESS, 
                ComplaintStatus.REOPENED,
                ComplaintStatus.RESOLVED
            ))
        );
    }

    private void logStatus(
            Complaint complaint,
            ComplaintStatus status,
            User user,
            String remarks) {

        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(status);
        history.setChangedBy(user);
        history.setChangedAt(LocalDateTime.now());
        history.setRemarks(remarks);
        historyRepository.save(history);
    }
}
