package com.example.CivicConnect.service.citizencomplaint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.NotificationRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ComplaintAssignmentService {

    private final OfficerProfileRepository officerProfileRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;

    public ComplaintAssignmentService(
            OfficerProfileRepository officerProfileRepository,
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationRepository notificationRepository) {

        this.officerProfileRepository = officerProfileRepository;
        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
    }

    // =====================================================
    // AUTO ASSIGN DURING COMPLAINT CREATION
    // =====================================================
    public void assignOfficer(Complaint complaint) {

        Optional<OfficerProfile> officerOpt =
                officerProfileRepository
                        .findFirstByWard_WardIdAndDepartment_DepartmentIdAndActiveTrueOrderByActiveComplaintCountAsc(
                                complaint.getWard().getWardId(),
                                complaint.getDepartment().getDepartmentId()
                        );

        // ❌ No department officer available
        if (officerOpt.isEmpty()) {
            notifyWardOfficer(complaint);
            return;
        }

        assignComplaintToOfficer(complaint, officerOpt.get());
    }

    // =====================================================
    // AUTO ASSIGN WHEN DEPARTMENT OFFICER IS ADDED
    // =====================================================
    public void assignPendingComplaintsForOfficer(OfficerProfile officerProfile) {

        // ✅ SAFETY: only department officers trigger assignment
        if (officerProfile.getDepartment() == null) {
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

    // =====================================================
    // COMMON ASSIGNMENT LOGIC (REUSED)
    // =====================================================
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

        // 🧾 STATUS HISTORY
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(ComplaintStatus.ASSIGNED);
        history.setChangedBy(officerProfile.getUser());
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);

        // 🔔 NOTIFY CITIZEN
        notifyCitizen(
                complaint,
                "Your complaint has been assigned to a department officer"
        );

        // 🔔 NOTIFY DEPARTMENT OFFICER
        notifyOfficer(
                officerProfile,
                "New complaint assigned. Complaint ID: " + complaint.getComplaintId()
        );
    }

    // =====================================================
    // NOTIFY WARD OFFICER (WHEN NO DEPT OFFICER EXISTS)
    // =====================================================
    private void notifyWardOfficer(Complaint complaint) {

        officerProfileRepository
                .findFirstByWard_WardIdAndUser_RoleAndActiveTrue(
                        complaint.getWard().getWardId(),
                        RoleName.WARD_OFFICER
                )
                .ifPresent(wardOfficer -> {

                    Notification notification = new Notification();
                    notification.setUser(wardOfficer.getUser());
                    notification.setMessage(
                            "No department officer available for complaint ID "
                                    + complaint.getComplaintId()
                                    + " (" + complaint.getDepartment().getName() + ")"
                    );
                    notification.setSeen(false);
                    notification.setCreatedAt(LocalDateTime.now());

                    notificationRepository.save(notification);
                });
    }

    // =====================================================
    // NOTIFY CITIZEN
    // =====================================================
    private void notifyCitizen(Complaint complaint, String message) {

        Notification notification = new Notification();
        notification.setUser(complaint.getCitizen());
        notification.setMessage(message);
        notification.setSeen(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    // =====================================================
    // NOTIFY DEPARTMENT OFFICER
    // =====================================================
    private void notifyOfficer(OfficerProfile officerProfile, String message) {

        Notification notification = new Notification();
        notification.setUser(officerProfile.getUser());
        notification.setMessage(message);
        notification.setSeen(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }
}
