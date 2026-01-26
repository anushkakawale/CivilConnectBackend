package com.example.CivicConnect.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.NotificationRepository;
import com.example.CivicConnect.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ===============================
    // CORE NOTIFICATION CREATOR
    // ===============================
    public void createNotification(
            User user,
            String title,
            String message,
            Long referenceId,
            NotificationType type) {

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    // ===============================
    // CITIZEN
    // ===============================
    public void notifyCitizen(
            User citizen,
            String title,
            String message,
            Long referenceId,
            NotificationType type) {

        createNotification(citizen, title, message, referenceId, type);
    }

    // ===============================
    // OFFICER
    // ===============================
    public void notifyOfficer(
            User officer,
            String title,
            String message,
            Long referenceId,
            NotificationType type) {

        createNotification(officer, title, message, referenceId, type);
    }

    // ===============================
    // WARD OFFICERS (BY WARD ID)
    // ===============================
    public void notifyWardOfficer(
            Long wardId,
            String title,
            String message,
            Long referenceId,
            NotificationType type) {

        List<User> wardOfficers =
                userRepository.findByRoleAndWard_WardId(
                        com.example.CivicConnect.entity.enums.RoleName.WARD_OFFICER,
                        wardId
                );

        wardOfficers.forEach(officer ->
                createNotification(officer, title, message, referenceId, type)
        );
    }

    // ===============================
    // SIMPLE USER MESSAGE (NO REFERENCE)
    // ===============================
    public void notifyUser(User user, String message) {
        createNotification(
                user,
                "Notification",
                message,
                null,
                NotificationType.SYSTEM
        );
    }
}