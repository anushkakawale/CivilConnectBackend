package com.example.CivicConnect.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ComplaintStatusService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;

    // ✅ Constructor Injection (MANDATORY for final fields)
    public ComplaintStatusService(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository) {
        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
    }

    // =====================================================
    // UPDATE COMPLAINT STATUS
    // =====================================================
    public void updateStatus(
            Long complaintId,
            ComplaintStatus newStatus,
            User actor) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // 🔐 Validate allowed transitions
        validateTransition(complaint.getStatus(), newStatus);

        // 🔄 Update complaint
        complaint.setStatus(newStatus);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setLastUpdatedBy(actor);

        // Auto close timestamp
        if (newStatus == ComplaintStatus.CLOSED) {
            complaint.setClosedAt(LocalDateTime.now());
            complaint.setClosedByAdmin(actor);
        }

        complaintRepository.save(complaint);

        // 🧾 Status History Entry
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(newStatus);
        history.setChangedBy(actor);
        history.setSystemGenerated(false);
        history.setChangedAt(LocalDateTime.now());

        historyRepository.save(history);
    }

    // =====================================================
    // VALIDATE STATUS FLOW
    // =====================================================
    private void validateTransition(
            ComplaintStatus current,
            ComplaintStatus next) {

        // SUBMITTED → ASSIGNED
        if (current == ComplaintStatus.SUBMITTED &&
            next == ComplaintStatus.ASSIGNED) return;

        // ASSIGNED → IN_PROGRESS
        if (current == ComplaintStatus.ASSIGNED &&
            next == ComplaintStatus.IN_PROGRESS) return;

        // IN_PROGRESS → RESOLVED
        if (current == ComplaintStatus.IN_PROGRESS &&
            next == ComplaintStatus.RESOLVED) return;

        // RESOLVED → CLOSED
        if (current == ComplaintStatus.RESOLVED &&
            next == ComplaintStatus.CLOSED) return;

        // ANY → REJECTED
        if (next == ComplaintStatus.REJECTED) return;

        throw new RuntimeException(
                "Invalid status transition: " + current + " → " + next
        );
    }
}
