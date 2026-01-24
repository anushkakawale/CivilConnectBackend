package com.example.CivicConnect.service.citizencomplaint;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CitizenComplaintActionService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final ComplaintSlaRepository slaRepository;

    public CitizenComplaintActionService(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            ComplaintSlaRepository slaRepository) {

        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.slaRepository = slaRepository;
    }

    public void reopenComplaint(Long complaintId, Long citizenUserId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // 🔐 Ownership check
        if (!complaint.getCitizen().getUserId().equals(citizenUserId)) {
            throw new RuntimeException("Access denied");
        }

        // 🔒 Status check
        if (!(complaint.getStatus() == ComplaintStatus.RESOLVED ||
              complaint.getStatus() == ComplaintStatus.CLOSED)) {
            throw new RuntimeException("Complaint cannot be reopened");
        }

        // ⏱ 7-day reopen window
        if (complaint.getClosedAt() != null &&
            complaint.getClosedAt().isBefore(LocalDateTime.now().minusDays(7))) {
            throw new RuntimeException("Reopen window expired");
        }

        // 🔄 Update status
        complaint.setStatus(ComplaintStatus.REOPENED);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        // 🧾 Status history
        ComplaintStatusHistory h = new ComplaintStatusHistory();
        h.setComplaint(complaint);
        h.setStatus(ComplaintStatus.REOPENED);
        h.setChangedBy(complaint.getCitizen());
        h.setSystemGenerated(false);
        h.setChangedAt(LocalDateTime.now());
        historyRepository.save(h);

        // 🔁 Restart SLA
        slaRepository.findByComplaint(complaint).ifPresent(sla -> {
            sla.setSlaStartTime(LocalDateTime.now());
            sla.setSlaDeadline(LocalDateTime.now().plusHours(
                    complaint.getDepartment().getSlaHours()
            ));
            sla.setEscalated(false);
            slaRepository.save(sla);
        });
    }
}
