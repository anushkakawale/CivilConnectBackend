package com.example.CivicConnect.service.admincomplaint;

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
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;

    public AdminComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationRepository notificationRepository) {

        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
    }

    // ▶ APPROVED → CLOSED
    public void closeComplaint(Long complaintId, Long adminUserId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // 🔒 Only APPROVED complaints can be closed
        if (complaint.getStatus() != ComplaintStatus.APPROVED) {
            throw new RuntimeException("Only APPROVED complaints can be closed");
        }

        complaint.setStatus(ComplaintStatus.CLOSED);
        complaintRepository.save(complaint);

        // 🧾 STATUS HISTORY
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(ComplaintStatus.CLOSED);
        history.setChangedBy(null); // or admin user if you store it
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);

        // 🔔 NOTIFY CITIZEN
        Notification notification = new Notification();
        notification.setUser(complaint.getCitizen());
        notification.setMessage(
                "Your complaint has been CLOSED by the administration"
        );
        notification.setSeen(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}