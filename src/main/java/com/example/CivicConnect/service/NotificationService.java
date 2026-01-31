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
import com.example.CivicConnect.entity.system.NotificationStats;
import com.example.CivicConnect.repository.NotificationRepository;
import com.example.CivicConnect.repository.NotificationStatsRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationStatsRepository notificationStatsRepository;
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
                .seen(false)
                .build();

        notificationRepository.save(notification);
        
        // Update notification stats
        updateStatsOnCreate(user);
    }
    
    // ===============================
    // NOTIFICATION STATS MANAGEMENT
    // ===============================
    private void updateStatsOnCreate(User user) {
        NotificationStats stats = notificationStatsRepository.findByUser(user)
                .orElseGet(() -> {
                    NotificationStats newStats = NotificationStats.builder()
                            .user(user)
                            .totalNotifications(0L)
                            .unreadCount(0L)
                            .unseenCount(0L)
                            .build();
                    return notificationStatsRepository.save(newStats);
                });
        
        stats.incrementCounts();
        notificationStatsRepository.save(stats);
    }
    
    /**
     * Get or create notification stats for a user
     */
    public NotificationStats getOrCreateStats(User user) {
        return notificationStatsRepository.findByUser(user)
                .orElseGet(() -> {
                    // Create new stats and sync with actual counts
                    NotificationStats stats = NotificationStats.builder()
                            .user(user)
                            .totalNotifications(0L)
                            .unreadCount(0L)
                            .unseenCount(0L)
                            .build();
                    NotificationStats saved = notificationStatsRepository.save(stats);
                    syncStatsWithDatabase(user);
                    return notificationStatsRepository.findByUser(user).orElse(saved);
                });
    }
    
    /**
     * Sync stats with actual database counts (for data integrity)
     */
    @Transactional
    public void syncStatsWithDatabase(User user) {
        NotificationStats stats = getOrCreateStats(user);
        
        long totalCount = notificationRepository.findByUserOrderByCreatedAtDesc(user).size();
        long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
        long unseenCount = notificationRepository.countByUserAndSeenFalse(user);
        
        stats.setTotalNotifications(totalCount);
        stats.setUnreadCount(unreadCount);
        stats.setUnseenCount(unseenCount);
        
        notificationStatsRepository.save(stats);
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

        if (!notification.isSeen()) {
            notification.setSeen(true);
            notificationRepository.save(notification);
            
            // Update stats
            NotificationStats stats = getOrCreateStats(user);
            stats.decrementUnseenCount();
            notificationStatsRepository.save(stats);
        }
    }

    @Transactional
    public int markAllAsRead(User user) {
        // Log before update
        long beforeCount = notificationRepository.countByUserAndIsReadFalse(user);
        System.out.println("🔔 Before markAllAsRead - Unread count: " + beforeCount);
        
        // Update notifications
        int updatedCount = notificationRepository.markAllAsRead(user);
        System.out.println("🔔 Updated " + updatedCount + " notifications");
        
        // Force immediate database commit
        notificationRepository.flush();
        
        // Update stats
        NotificationStats stats = getOrCreateStats(user);
        stats.resetUnreadCount();
        notificationStatsRepository.save(stats);
        
        // Log after update
        long afterCount = notificationRepository.countByUserAndIsReadFalse(user);
        System.out.println("🔔 After markAllAsRead - Unread count: " + afterCount);
        
        return updatedCount;
    }

    @Transactional
    public int markAllAsSeen(User user) {
        int updatedCount = notificationRepository.markAllAsSeen(user);
        
        // Update stats
        NotificationStats stats = getOrCreateStats(user);
        stats.resetUnseenCount();
        notificationStatsRepository.save(stats);
        
        return updatedCount;
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
    // DELETE NOTIFICATIONS
    // ===============================
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public int clearReadNotifications(User user) {
        List<Notification> readNotifications = notificationRepository.findByUserAndIsReadTrue(user);
        notificationRepository.deleteAll(readNotifications);
        return readNotifications.size();
    }

    // ===============================
    // DTO MAPPING METHODS
    // ===============================
    public List<com.example.CivicConnect.dto.NotificationDTO> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<com.example.CivicConnect.dto.NotificationDTO> getUnreadNotificationDTOs(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private com.example.CivicConnect.dto.NotificationDTO mapToDTO(Notification n) {
        return new com.example.CivicConnect.dto.NotificationDTO(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getReferenceId(),
                n.getCreatedAt(),
                n.isRead(),
                calculateTimeElapsed(n.getCreatedAt())
        );
    }

    private String calculateTimeElapsed(java.time.LocalDateTime createdAt) {
        if (createdAt == null) return "just now";
        
        java.time.Duration duration = java.time.Duration.between(createdAt, java.time.LocalDateTime.now());
        long seconds = duration.getSeconds();
        
        if (seconds < 60) return "just now";
        if (seconds < 3600) return (seconds / 60) + " min ago";
        if (seconds < 86400) return (seconds / 3600) + " hr ago";
        return (seconds / 86400) + " day(s) ago";
    }
    
    // ===============================
    // NOTIFICATION STATS DTO
    // ===============================
    public com.example.CivicConnect.dto.NotificationStatsDTO getNotificationStats(User user) {
        NotificationStats stats = getOrCreateStats(user);
        
        return com.example.CivicConnect.dto.NotificationStatsDTO.builder()
                .totalNotifications(stats.getTotalNotifications())
                .unreadCount(stats.getUnreadCount())
                .unseenCount(stats.getUnseenCount())
                .build();
    }
}
