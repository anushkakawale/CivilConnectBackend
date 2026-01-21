package com.example.CivicConnect.service.departmentcomplaint;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;

@Service
@Transactional
public class DepartmentComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;

    public DepartmentComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository) {

        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
    }

    // ▶ START WORK ON COMPLAINT
    public void startWork(Long complaintId, User officer) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // 🔐 Validate assignment
        if (complaint.getAssignedOfficer() == null ||
            !complaint.getAssignedOfficer().getUserId().equals(officer.getUserId())) {

            throw new RuntimeException("You are not assigned to this complaint");
        }

        if (complaint.getStatus() != ComplaintStatus.ASSIGNED) {
            throw new RuntimeException("Complaint cannot be started in current state");
        }

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);

        logStatus(complaint, ComplaintStatus.IN_PROGRESS, officer);
    }

    // ▶ RESOLVE COMPLAINT
    public void resolve(Long complaintId, User officer) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (complaint.getAssignedOfficer() == null ||
            !complaint.getAssignedOfficer().getUserId().equals(officer.getUserId())) {

            throw new RuntimeException("You are not assigned to this complaint");
        }

        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            throw new RuntimeException("Complaint must be IN_PROGRESS to resolve");
        }

        complaint.setStatus(ComplaintStatus.CLOSED);

        logStatus(complaint, ComplaintStatus.CLOSED, officer);
    }

    // ▶ STATUS HISTORY
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
