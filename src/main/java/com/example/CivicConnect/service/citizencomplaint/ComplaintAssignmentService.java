package com.example.CivicConnect.service.citizencomplaint;

import java.time.LocalDateTime;
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

    public void assignOfficer(Complaint complaint) {

        Optional<OfficerProfile> officerOpt =
                officerProfileRepository
                        .findFirstByWard_WardIdAndDepartment_DepartmentIdAndActiveTrueOrderByActiveComplaintCountAsc(
                                complaint.getWard().getWardId(),
                                complaint.getDepartment().getDepartmentId()
                        );

        // 🚨 NO DEPARTMENT OFFICER FOUND
        if (officerOpt.isEmpty()) {
            notifyWardOfficer(complaint);
            return;
        }

        // ✅ ASSIGN DEPARTMENT OFFICER
        OfficerProfile officerProfile = officerOpt.get();

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

        historyRepository.save(history);
    }

    // 🔔 NOTIFY WARD OFFICER
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
}
