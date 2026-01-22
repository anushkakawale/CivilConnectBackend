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
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.NotificationRepository;

@Service
@Transactional
public class WardOfficerComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintApprovalRepository approvalRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;

    public WardOfficerComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintApprovalRepository approvalRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationRepository notificationRepository) {

        this.complaintRepository = complaintRepository;
        this.approvalRepository = approvalRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
    }

    // ▶ APPROVE
    public void approve(Long complaintId, User wardOfficer, String remarks) {

        Complaint complaint = getComplaint(complaintId);
        ComplaintApproval approval = getPendingApproval(complaint);

        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setDecidedBy(wardOfficer);
        approval.setRemarks(remarks);
        approval.setDecidedAt(LocalDateTime.now());

        complaint.setStatus(ComplaintStatus.APPROVED);

        approvalRepository.save(approval);
        logStatus(complaint, ComplaintStatus.APPROVED, wardOfficer);

    }

    // ▶ REJECT → BACK TO DEPARTMENT
    public void reject(Long complaintId, User wardOfficer, String remarks) {

        Complaint complaint = getComplaint(complaintId);
        ComplaintApproval approval = getPendingApproval(complaint);

        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setDecidedBy(wardOfficer);
        approval.setRemarks(remarks);
        approval.setDecidedAt(LocalDateTime.now());

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);

        approvalRepository.save(approval);
        logStatus(complaint, ComplaintStatus.IN_PROGRESS, wardOfficer);

        notifyDepartmentOfficer(complaint, remarks);
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

    private void notifyDepartmentOfficer(
            Complaint complaint, String remarks) {

        Notification n = new Notification();
        n.setUser(complaint.getAssignedOfficer());
        n.setMessage(
                "Complaint rejected by Ward Officer. Reason: " + remarks);
        n.setSeen(false);
        n.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(n);
    }
}
