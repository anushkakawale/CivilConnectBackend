package com.example.CivicConnect.service.wardcomplaint;

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
public class WardOfficerComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;

    public WardOfficerComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationRepository notificationRepository) {

        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
    }

 // ▶ RESOLVED → APPROVED
    //ward officer verifies work , and then approves
    public void approveComplaint(Long complaintId, Long wardOfficerUserId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // ✅ CORRECT CHECK
        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new RuntimeException(
                "Only RESOLVED complaints can be approved by Ward Officer"
            );
        }

        complaint.setStatus(ComplaintStatus.APPROVED);
        complaintRepository.save(complaint);

        // 🧾 STATUS HISTORY
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(ComplaintStatus.APPROVED);
        history.setChangedBy(null);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);

        // 🔔 NOTIFY ADMIN
        Notification adminNotification = new Notification();
        adminNotification.setUser(null);
        adminNotification.setMessage(
            "Complaint ID " + complaint.getComplaintId()
            + " approved by Ward Officer and ready for closure"
        );
        adminNotification.setSeen(false);
        adminNotification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(adminNotification);
    }

}