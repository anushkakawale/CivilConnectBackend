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

        if (officerOpt.isEmpty()) {
            notifyWardOfficer(complaint);
            return;
        }

        assignComplaintToOfficer(complaint, officerOpt.get());
    }

    // =====================================================
    // AUTO ASSIGN PENDING COMPLAINTS WHEN OFFICER IS ADDED
    // =====================================================
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

    // =====================================================
    // COMMON ASSIGNMENT LOGIC
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

        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(ComplaintStatus.ASSIGNED);
        history.setChangedBy(officerProfile.getUser());
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);

    }

//        notificationService.notifyOfficer(
//                officerProfile.getUser(),
//                "New complaint assigned: " + complaint.getTitle(),
//                complaint.getComplaintId()
//        );
//        notificationService.notifyCitizen(
//        	    officerProfile,
//        	    "New complaint assigned",
//        	    "Complaint ID " + complaint.getComplaintId(),
//        	    complaint.getComplaintId(),
//        	    NotificationType.ASSIGNMENT
//        	);


    // =====================================================
    // WARD OFFICER NOTIFICATION
    // =====================================================
    private void notifyWardOfficer(Complaint complaint) {

        officerProfileRepository
                .findFirstByWard_WardIdAndUser_RoleAndActiveTrue(
                        complaint.getWard().getWardId(),
                        RoleName.WARD_OFFICER
                )
                .ifPresent(wardOfficer ->
                notificationService.notifyOfficer(
                	    wardOfficer.getUser(),
                	    "Assignment Failed",
                	    "No department officer available for complaint: " + complaint.getTitle(),
                	    complaint.getComplaintId(),
                	    NotificationType.ASSIGNMENT
                ));
    }
}
