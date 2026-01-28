package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.NotificationRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final OfficerProfileRepository officerProfileRepository;

    // ===============================
    // CORE NOTIFICATION CREATOR (Using Builder)
    // ===============================
    private void createNotification(
            User user,
            String title,
            String message,
            Long referenceId,
            NotificationType type,
            RoleName targetRole) {

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .type(type)
                .targetRole(targetRole)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    // ===============================
    // NEW COMPLAINT NOTIFICATION
    // ===============================
    public void notifyNewComplaint(Complaint complaint, User assignedOfficer) {
        // Notify citizen
        createNotification(
                complaint.getCitizen(),
                "Complaint Registered",
                "Your complaint #" + complaint.getComplaintId() + " has been registered successfully.",
                complaint.getComplaintId(),
                NotificationType.COMPLAINT_CREATED,
                RoleName.CITIZEN
        );

        // Notify assigned officer if exists
        if (assignedOfficer != null) {
            createNotification(
                    assignedOfficer,
                    "New Complaint Assigned",
                    "Complaint #" + complaint.getComplaintId() + " has been assigned to you.",
                    complaint.getComplaintId(),
                    NotificationType.NEW_COMPLAINT,
                    assignedOfficer.getRole()
            );
        }
    }

    // ===============================
    // COMPLAINT APPROVED
    // ===============================
    public void notifyComplaintApproved(Complaint complaint) {
        createNotification(
                complaint.getCitizen(),
                "Complaint Approved",
                "Your complaint #" + complaint.getComplaintId() + " has been approved and is being processed.",
                complaint.getComplaintId(),
                NotificationType.APPROVAL_REQUIRED,
                RoleName.CITIZEN
        );
    }

    // ===============================
    // COMPLAINT ASSIGNED
    // ===============================
    public void notifyComplaintAssigned(Complaint complaint, User officer) {
        // Notify officer
        createNotification(
                officer,
                "Complaint Assigned",
                "Complaint #" + complaint.getComplaintId() + " has been assigned to you.",
                complaint.getComplaintId(),
                NotificationType.ASSIGNMENT,
                officer.getRole()
        );

        // Notify citizen
        createNotification(
                complaint.getCitizen(),
                "Complaint Assigned",
                "Your complaint #" + complaint.getComplaintId() + " has been assigned to an officer.",
                complaint.getComplaintId(),
                NotificationType.STATUS_UPDATE,
                RoleName.CITIZEN
        );
    }

    // ===============================
    // STATUS UPDATE
    // ===============================
    public void notifyStatusUpdate(Complaint complaint, String statusMessage) {
        createNotification(
                complaint.getCitizen(),
                "Status Update",
                "Complaint #" + complaint.getComplaintId() + ": " + statusMessage,
                complaint.getComplaintId(),
                NotificationType.STATUS_UPDATE,
                RoleName.CITIZEN
        );
    }

    // ===============================
    // COMPLAINT RESOLVED
    // ===============================
    public void notifyComplaintResolved(Complaint complaint) {
        createNotification(
                complaint.getCitizen(),
                "Complaint Resolved",
                "Your complaint #" + complaint.getComplaintId() + " has been marked as resolved.",
                complaint.getComplaintId(),
                NotificationType.RESOLVED,
                RoleName.CITIZEN
        );
    }

    // ===============================
    // COMPLAINT CLOSED
    // ===============================
    public void notifyComplaintClosed(Complaint complaint) {
        createNotification(
                complaint.getCitizen(),
                "Complaint Closed",
                "Your complaint #" + complaint.getComplaintId() + " has been closed.",
                complaint.getComplaintId(),
                NotificationType.CLOSED,
                RoleName.CITIZEN
        );
    }

    // ===============================
    // COMPLAINT REOPENED
    // ===============================
    public void notifyComplaintReopened(Complaint complaint) {
        createNotification(
                complaint.getCitizen(),
                "Complaint Reopened",
                "Your complaint #" + complaint.getComplaintId() + " has been reopened.",
                complaint.getComplaintId(),
                NotificationType.REOPENED,
                RoleName.CITIZEN
        );
    }

    // ===============================
    // SLA WARNING
    // ===============================
    public void notifySLAWarning(Complaint complaint, User officer) {
        createNotification(
                officer,
                "SLA Warning",
                "Complaint #" + complaint.getComplaintId() + " is approaching SLA deadline.",
                complaint.getComplaintId(),
                NotificationType.SLA_WARNING,
                officer.getRole()
        );
    }

    // ===============================
    // SLA BREACHED
    // ===============================
    public void notifySLABreached(Complaint complaint, User officer) {
        createNotification(
                officer,
                "SLA Breached",
                "Complaint #" + complaint.getComplaintId() + " has breached SLA deadline!",
                complaint.getComplaintId(),
                NotificationType.SLA_BREACHED,
                officer.getRole()
        );
    }

    // ===============================
    // FETCH NOTIFICATIONS
    // ===============================
    public List<Notification> getAllNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Page<Notification> getNotificationsPaginated(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public long getUnseenCount(User user) {
        return notificationRepository.countByUserAndSeenFalse(user);
    }

    // ===============================
    // MARK AS READ/SEEN
    // ===============================
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAsSeen(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsRead(user);
    }

    @Transactional
    public int markAllAsSeen(User user) {
        return notificationRepository.markAllAsSeen(user);
    }

    // ===============================
    // GENERIC NOTIFICATION
    // ===============================
    public void notifyUser(User user, String title, String message) {
        createNotification(
                user,
                title,
                message,
                null,
                NotificationType.SYSTEM,
                user.getRole()
        );
    }

    public void notifyUser(User user, String message) {
        notifyUser(user, "Notification", message);
    }
    
    // ===============================
    // OFFICER NOTIFICATION (BY USER OBJ)
    // ===============================
    public void notifyOfficer(User officer, String title, String message, Long referenceId, NotificationType type) {
        createNotification(
                officer,
                title,
                message,
                referenceId,
                type,
                officer.getRole()
        );
    }

    // ===============================
    // CITIZEN NOTIFICATION
    // ===============================
    public void notifyCitizen(User citizen, String title, String message, Long referenceId, NotificationType type) {
        createNotification(
                citizen,
                title,
                message,
                referenceId,
                type,
                RoleName.CITIZEN
        );
    }

    // ===============================
    // WARD OFFICER NOTIFICATION (BY WARD ID)
    // ===============================
    public void notifyWardOfficer(Long wardId, String title, String message, Long referenceId, NotificationType type) {
        
        com.example.CivicConnect.entity.profiles.OfficerProfile warden = 
            officerProfileRepository.findFirstByWard_WardIdAndUser_RoleAndActiveTrue(
                wardId, 
                RoleName.WARD_OFFICER
            ).orElse(null);

        if (warden != null) {
            createNotification(
                warden.getUser(),
                title,
                message,
                referenceId,
                type,
                RoleName.WARD_OFFICER
            );
        }
    }
    // ===============================
    // DTO MAPPING METHODS
    // ===============================
    public List<com.example.CivicConnect.dto.NotificationDTO> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(n -> new com.example.CivicConnect.dto.NotificationDTO(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getType(),
                        n.getReferenceId(),
                        n.getCreatedAt(),
                        n.isRead()
                ))
                .toList();
    }
}
