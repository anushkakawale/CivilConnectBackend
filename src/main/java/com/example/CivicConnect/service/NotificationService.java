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
import com.example.CivicConnect.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationStatsRepository notificationStatsRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final UserRepository userRepository;

    private void createNotification(
            User user,
            String title,
            String message,
            Long referenceId,
            NotificationType type,
            RoleName targetRole) {

        if (user == null) {
            System.err.println("⚠️ ERROR: Attempted to create notification for NULL user. Title: " + title);
            return;
        }

        if (targetRole == null) {
            targetRole = (user.getRole() != null) ? user.getRole() : RoleName.CITIZEN;
        }

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
        updateStatsOnCreate(user);
    }
    
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
    
    public NotificationStats getOrCreateStats(User user) {
        return notificationStatsRepository.findByUser(user)
                .orElseGet(() -> {
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

    public void notifyNewComplaint(Complaint complaint, User assignedOfficer) {
        createNotification(
                complaint.getCitizen(),
                "Complaint Registered",
                "Your complaint #" + complaint.getComplaintId() + " has been registered successfully.",
                complaint.getComplaintId(),
                NotificationType.COMPLAINT_CREATED,
                RoleName.CITIZEN
        );

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

    public void notifyComplaintAssigned(Complaint complaint, User officer) {
        createNotification(
                officer,
                "Complaint Assigned",
                "Complaint #" + complaint.getComplaintId() + " has been assigned to you.",
                complaint.getComplaintId(),
                NotificationType.ASSIGNMENT,
                officer.getRole()
        );

        createNotification(
                complaint.getCitizen(),
                "Complaint Assigned",
                "Your complaint #" + complaint.getComplaintId() + " has been assigned to an officer.",
                complaint.getComplaintId(),
                NotificationType.STATUS_UPDATE,
                RoleName.CITIZEN
        );
    }

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

    public void notifyComplaintClosed(Complaint complaint, User admin) {
        createNotification(
                complaint.getCitizen(),
                "Complaint Closed",
                "Your complaint #" + complaint.getComplaintId() + " has been closed.",
                complaint.getComplaintId(),
                NotificationType.CLOSED,
                RoleName.CITIZEN
        );

        createNotification(
                admin,
                "Complaint Closed",
                "You successfully closed Complaint #" + complaint.getComplaintId(),
                complaint.getComplaintId(),
                NotificationType.CLOSED,
                RoleName.ADMIN
        );
    }

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

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            
            NotificationStats stats = getOrCreateStats(user);
            stats.decrementUnreadCount();
            notificationStatsRepository.save(stats);
        }
    }

    @Transactional
    public void markAsSeen(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        if (!notification.isSeen()) {
            notification.setSeen(true);
            notificationRepository.save(notification);
            
            NotificationStats stats = getOrCreateStats(user);
            stats.decrementUnseenCount();
            notificationStatsRepository.save(stats);
        }
    }

    @Transactional
    public int markAllAsRead(User user) {
        int updatedCount = notificationRepository.markAllAsRead(user);
        
        NotificationStats stats = getOrCreateStats(user);
        stats.resetUnreadCount();
        notificationStatsRepository.save(stats);
        
        return updatedCount;
    }

    @Transactional
    public int markAllAsSeen(User user) {
        int updatedCount = notificationRepository.markAllAsSeen(user);
        
        NotificationStats stats = getOrCreateStats(user);
        stats.resetUnseenCount();
        notificationStatsRepository.save(stats);
        
        return updatedCount;
    }

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
    
    public void notifyOfficer(User officer, String title, String message, Long referenceId, NotificationType type) {
        if (officer == null) {
            System.err.println("⚠️ Attempted to notify null officer for referenceId: " + referenceId);
            return;
        }
        createNotification(
                officer,
                title,
                message,
                referenceId,
                type,
                officer.getRole()
        );
    }

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

    public void notifyWardOfficer(Long wardId, String title, String message, Long referenceId, NotificationType type) {
        officerProfileRepository.findFirstByWard_WardIdAndUser_RoleAndActiveTrue(
                wardId, 
                RoleName.WARD_OFFICER
            ).ifPresent(warden -> {
                createNotification(
                    warden.getUser(),
                    title,
                    message,
                    referenceId,
                    type,
                    RoleName.WARD_OFFICER
                );
            });
    }

    public void notifyAdmins(String title, String message, Long referenceId, NotificationType type) {
        List<User> admins = userRepository.findByRole(RoleName.ADMIN);
        for (User admin : admins) {
            createNotification(
                    admin,
                    title,
                    message,
                    referenceId,
                    type,
                    RoleName.ADMIN
            );
        }
    }

    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        boolean wasRead = notification.isRead();
        boolean wasSeen = notification.isSeen();

        notificationRepository.delete(notification);

        NotificationStats stats = getOrCreateStats(user);
        stats.setTotalNotifications(Math.max(0, stats.getTotalNotifications() - 1));
        if (!wasRead) stats.decrementUnreadCount();
        if (!wasSeen) stats.decrementUnseenCount();
        notificationStatsRepository.save(stats);
    }

    @Transactional
    public int clearReadNotifications(User user) {
        List<Notification> readNotifications = notificationRepository.findByUserAndIsReadTrue(user);
        int count = readNotifications.size();
        notificationRepository.deleteAll(readNotifications);
        
        syncStatsWithDatabase(user);
        
        return count;
    }

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
    
    @Transactional
    public com.example.CivicConnect.dto.NotificationStatsDTO getNotificationStats(User user) {
        syncStatsWithDatabase(user);
        
        NotificationStats stats = notificationStatsRepository.findByUser(user)
                .orElseGet(() -> getOrCreateStats(user));
        
        return com.example.CivicConnect.dto.NotificationStatsDTO.builder()
                .totalNotifications(stats.getTotalNotifications())
                .unreadCount(stats.getUnreadCount())
                .unseenCount(stats.getUnseenCount())
                .build();
    }
}
