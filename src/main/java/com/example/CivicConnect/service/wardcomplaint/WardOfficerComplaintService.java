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
    private final UserRepository userRepository; // ✅ ADD
    public WardOfficerComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintApprovalRepository approvalRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationService notificationService,
            UserRepository userRepository) {   // ✅ ADD

        this.complaintRepository = complaintRepository;
        this.approvalRepository = approvalRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }
    public void approve(Long complaintId, User wardOfficer, String remarks) {

        Complaint complaint = getComplaint(complaintId);
        ComplaintApproval approval = getPendingApproval(complaint);

        // 1️⃣ Update approval
        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setDecidedBy(wardOfficer);
        approval.setRemarks(remarks);
        approval.setDecidedAt(LocalDateTime.now());

        // 2️⃣ Update complaint status
        complaint.setStatus(ComplaintStatus.APPROVED);

        approvalRepository.save(approval);
        complaintRepository.save(complaint);

        // 3️⃣ Log history
        logStatus(complaint, ComplaintStatus.APPROVED, wardOfficer);

        // 4️⃣ 🔔 NOTIFY ADMIN (IMPORTANT PART)
        User admin =
            userRepository.findFirstByRole(RoleName.ADMIN)
                .orElseThrow(() ->
                    new RuntimeException("Admin user not found"));

        notificationService.notifyUser(
            admin,
            "Complaint ID " + complaint.getComplaintId()
                + " approved by Ward Officer and ready for closure"
        );
    }

 // ▶ REJECT → BACK TO DEPARTMENT
    public void reject(Long complaintId, User wardOfficer, String remarks) {

        Complaint complaint = getComplaint(complaintId);
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
        logStatus(complaint, ComplaintStatus.IN_PROGRESS, wardOfficer);

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

    private Complaint getComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Complaint not found"));
    }

    private void logStatus(
            Complaint complaint,
            ComplaintStatus status,
            User user) {

        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(status);
        history.setChangedBy(user);
        history.setChangedAt(LocalDateTime.now());
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
