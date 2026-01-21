package com.example.CivicConnect.service.wardcomplaint;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

@Service
@Transactional
public class WardOfficerComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public WardOfficerComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            OfficerProfileRepository officerProfileRepository) {

        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.officerProfileRepository = officerProfileRepository;
    }

    // ▶ APPROVE COMPLAINT
    public void approveComplaint(Long complaintId, User wardOfficer) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new RuntimeException("Only COMPLETED complaints can be approved");
        }

        // 🔐 FETCH OFFICER PROFILE SAFELY
        OfficerProfile officerProfile =
                officerProfileRepository
                        .findByUser_UserId(wardOfficer.getUserId())
                        .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        // 🔐 WARD OWNERSHIP CHECK
        if (!complaint.getWard().getWardId()
                .equals(officerProfile.getWard().getWardId())) {

            throw new RuntimeException("Complaint does not belong to your ward");
        }

        complaint.setStatus(ComplaintStatus.APPROVED);

        logStatus(complaint, ComplaintStatus.APPROVED, wardOfficer);
    }

    private void logStatus(Complaint complaint,
                           ComplaintStatus status,
                           User changedBy) {

        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(status);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());

        historyRepository.save(history);
    }
}
