package com.example.CivicConnect.service.wardcomplaint;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
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
    private final com.example.CivicConnect.repository.OfficerProfileRepository officerProfileRepository;

    public WardOfficerComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintApprovalRepository approvalRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationService notificationService,
            UserRepository userRepository,
            com.example.CivicConnect.repository.OfficerProfileRepository officerProfileRepository) {

        this.complaintRepository = complaintRepository;
        this.approvalRepository = approvalRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.officerProfileRepository = officerProfileRepository;
    }
    
    public void approve(Long complaintId, User wardOfficer, String remarks) {

        Complaint complaint = getComplaint(complaintId);
        
        // 🔒 WARD SECURITY CHECK (MANDATORY)
        var profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
        		.orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        if (!complaint.getWard().getWardId().equals(profile.getWard().getWardId())) {
        	throw new RuntimeException("ACCESS DENIED: You cannot act on complaints from other wards");
        }
        
        ComplaintApproval approval = getPendingApproval(complaint);

        // 1️⃣ Update approval
        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setDecidedBy(wardOfficer);
        approval.setRemarks(remarks);
        approval.setDecidedAt(LocalDateTime.now());

        // 2️⃣ Update complaint status
        complaint.setStatus(ComplaintStatus.APPROVED);
        complaint.setApprovedBy(wardOfficer);
        complaint.setApprovedAt(LocalDateTime.now());

        approvalRepository.save(approval);
        complaintRepository.save(complaint);

        // 3️⃣ Log history
        logStatus(complaint, ComplaintStatus.APPROVED, wardOfficer, remarks);

        // 4️⃣ 🔔 NOTIFY ADMIN (IMPORTANT PART)
        User admin =
            userRepository.findFirstByRole(RoleName.ADMIN)
                .orElseThrow(() ->
                    new RuntimeException("Admin user not found"));

        notificationService.notifyUser(
            admin,
            "Complaint Approved",
            "Complaint ID " + complaint.getComplaintId()
                + " approved by Ward Officer and ready for closure"
        );
    }

 // ▶ REJECT → BACK TO DEPARTMENT
    public void reject(Long complaintId, User wardOfficer, String remarks) {

        Complaint complaint = getComplaint(complaintId);
        
        // 🔒 WARD SECURITY CHECK
        var profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
        		.orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        if (!complaint.getWard().getWardId().equals(profile.getWard().getWardId())) {
        	throw new RuntimeException("ACCESS DENIED: You cannot act on complaints from other wards");
        }

        ComplaintApproval approval = getPendingApproval(complaint);

        // 1️⃣ Update approval
        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setDecidedBy(wardOfficer);
        approval.setRemarks(remarks);
        approval.setDecidedAt(LocalDateTime.now());

        // 2️⃣ Send complaint back to department
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);

        approvalRepository.save(approval);
        complaintRepository.save(complaint);

        // 3️⃣ Log history
        logStatus(complaint, ComplaintStatus.IN_PROGRESS, wardOfficer, "Rejected by Ward Officer: " + remarks);

        // 4️⃣ 🔔 Notify Department Officer (CORRECT WAY)
        notificationService.notifyOfficer(
        	    complaint.getAssignedOfficer(),
        	    "Complaint Rejected",
        	    "Rejected by Ward Officer. Reason: " + remarks,
        	    complaint.getComplaintId(),
        	    NotificationType.STATUS_UPDATE
        	);

    }


    private ComplaintApproval getPendingApproval(Complaint complaint) {
        return approvalRepository
                .findByComplaintAndStatus(
                        complaint, ApprovalStatus.PENDING)
                .orElseThrow(() ->
                        new RuntimeException("No pending approval found"));
    }

    // ✅ NEW: ASSIGN OFFICER
    public void assignOfficer(Long complaintId, Long officerId, User wardOfficer) {

        Complaint complaint = getComplaint(complaintId);

        // 🔒 SECURITY CHECK
        var profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
        		.orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        if (!complaint.getWard().getWardId().equals(profile.getWard().getWardId())) {
        	throw new RuntimeException("ACCESS DENIED: You cannot act on complaints from other wards");
        }

        User newOfficer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Target officer not found"));

        if (!newOfficer.getRole().equals(RoleName.DEPARTMENT_OFFICER)) {
            throw new RuntimeException("Can only assign to Department Officer");
        }

        complaint.setAssignedOfficer(newOfficer);
        complaint.setStatus(ComplaintStatus.ASSIGNED); // Or keep existing? Usually explicit assignment means ASSIGNED.
        complaintRepository.save(complaint);

        // Log history
        logStatus(complaint, ComplaintStatus.ASSIGNED, wardOfficer, "Re-assigned to " + newOfficer.getName());

        // Notify new officer
        notificationService.notifyOfficer(
                newOfficer,
                "Complaint Assigned",
                "Ward Officer assigned Complaint #" + complaintId + " to you.",
                complaintId,
                NotificationType.ASSIGNMENT
        );
    }

    private Complaint getComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Complaint not found"));
    }

    public org.springframework.data.domain.Page<Complaint> getWardComplaints(Long wardId, org.springframework.data.domain.Pageable pageable) {
        return complaintRepository.findByWard_WardId(wardId, pageable);
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
    

//    private void notifyDepartmentOfficer(
//            Complaint complaint, String remarks) {
//
//        Notification n = new Notification();
//        n.setUser(complaint.getAssignedOfficer());
//        n.setMessage(
//                "Complaint rejected by Ward Officer. Reason: " + remarks);
//        n.setSeen(false);
//        n.setCreatedAt(LocalDateTime.now());
//
//        notificationRepository.save(n);
//    }
}
