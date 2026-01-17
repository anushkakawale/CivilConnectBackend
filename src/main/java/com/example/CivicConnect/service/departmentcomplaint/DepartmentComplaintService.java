package com.example.CivicConnect.service.departmentcomplaint;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.NotificationRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DepartmentComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;

    public DepartmentComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationRepository notificationRepository) {

        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
    }

    // ▶ ASSIGNED → IN_PROGRESS
    public void startWork(Long complaintId, Long officerUserId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Authorization check
        if (complaint.getAssignedOfficer() == null ||
            !complaint.getAssignedOfficer().getUserId().equals(officerUserId)) {
            throw new RuntimeException("Not authorized");
        }

        if (complaint.getStatus() != ComplaintStatus.ASSIGNED) {
            throw new RuntimeException("Complaint must be ASSIGNED");
        }

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintRepository.save(complaint);

        logHistory(complaint, ComplaintStatus.IN_PROGRESS);
        notifyCitizen(complaint, "Work started on your complaint");
    }

    // ▶ IN_PROGRESS → RESOLVED
    public void resolve(Long complaintId, Long officerUserId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (complaint.getAssignedOfficer() == null ||
            !complaint.getAssignedOfficer().getUserId().equals(officerUserId)) {
            throw new RuntimeException("Not authorized");
        }

        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            throw new RuntimeException("Complaint must be IN_PROGRESS");
        }

        complaint.setStatus(ComplaintStatus.APPROVED);
        complaintRepository.save(complaint);

        logHistory(complaint, ComplaintStatus.APPROVED);
        notifyCitizen(complaint, "Your complaint has been resolved");
    }

    // ---------------- HELPERS ----------------

    private void logHistory(Complaint complaint, ComplaintStatus status) {
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(status);
        history.setChangedBy(complaint.getAssignedOfficer());
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private void notifyCitizen(Complaint complaint, String message) {
        Notification notification = new Notification();
        notification.setUser(complaint.getCitizen());
        notification.setMessage(message);
        notification.setSeen(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}