package com.example.CivicConnect.service.citizencomplaint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ComplaintAssignmentService {

    private final OfficerProfileRepository officerProfileRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationService notificationService;

    public ComplaintAssignmentService(
            OfficerProfileRepository officerProfileRepository,
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationService notificationService) {

        this.officerProfileRepository = officerProfileRepository;
        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
    }

    public void assignOfficer(Complaint complaint) {
        Optional<OfficerProfile> officerOpt =
                officerProfileRepository
                        .findFirstByWard_WardIdAndDepartment_DepartmentIdAndActiveTrueOrderByActiveComplaintCountAsc(
                                complaint.getWard().getWardId(),
                                complaint.getDepartment().getDepartmentId()
                        );

        if (officerOpt.isEmpty()) {
            notifyWardOfficer(complaint);
            return;
        }

        assignComplaintToOfficer(complaint, officerOpt.get());
    }

    public void assignPendingComplaintsForOfficer(OfficerProfile officerProfile) {
        if (officerProfile == null ||
            officerProfile.getWard() == null ||
            officerProfile.getDepartment() == null) {
            return;
        }

        List<Complaint> pendingComplaints =
                complaintRepository.findByWard_WardIdAndDepartment_DepartmentIdAndStatus(
                        officerProfile.getWard().getWardId(),
                        officerProfile.getDepartment().getDepartmentId(),
                        ComplaintStatus.SUBMITTED
                );

        for (Complaint complaint : pendingComplaints) {
            assignComplaintToOfficer(complaint, officerProfile);
        }
    }

    private void assignComplaintToOfficer(
            Complaint complaint,
            OfficerProfile officerProfile) {

        complaint.setAssignedOfficer(officerProfile.getUser());
        complaint.setStatus(ComplaintStatus.ASSIGNED);

        officerProfile.setActiveComplaintCount(
                officerProfile.getActiveComplaintCount() + 1
        );

        complaintRepository.save(complaint);
        officerProfileRepository.save(officerProfile);

        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(ComplaintStatus.ASSIGNED);
        history.setChangedBy(officerProfile.getUser());
        history.setChangedAt(LocalDateTime.now());
        history.setRemarks("Auto-assigned to Department Officer: " + officerProfile.getUser().getName());
        historyRepository.save(history);

        // Notify both parties
        notificationService.notifyComplaintAssigned(complaint, officerProfile.getUser());
    }

    private void notifyWardOfficer(Complaint complaint) {
        Optional<OfficerProfile> wardOfficerOpt = officerProfileRepository
                .findFirstByWard_WardIdAndUser_RoleAndActiveTrue(
                        complaint.getWard().getWardId(),
                        RoleName.WARD_OFFICER
                );

        if (wardOfficerOpt.isPresent()) {
            notificationService.notifyOfficer(
                    wardOfficerOpt.get().getUser(),
                    "Action Required: Unassigned Complaint",
                    "A new complaint (ID #" + complaint.getComplaintId() + ") lacks an assigned Department Officer. Please review and assign manually.",
                    complaint.getComplaintId(),
                    NotificationType.ASSIGNMENT
            );
        } else {
            notificationService.notifyAdmins(
                    "Critical Escalation: Unassigned Complaint",
                    "Complaint #" + complaint.getComplaintId() + " in Ward " + complaint.getWard().getAreaName() + " has NO available officers (Ward or Dept). Immediate attention required.",
                    complaint.getComplaintId(),
                    NotificationType.ESCALATION
            );
        }
    }
}
