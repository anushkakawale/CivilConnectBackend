package com.example.CivicConnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.NotificationDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
            notificationService.getNotificationsForUser(user)
        );
    }

    /**
     * Get only unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
            notificationService.getUnreadNotificationDTOs(user)
        );
    }

    /**
     * Get unread count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
    
    /**
     * Get notification statistics (total, unread, unseen)
     */
    @GetMapping("/stats")
    public ResponseEntity<com.example.CivicConnect.dto.NotificationStatsDTO> getNotificationStats(
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotificationStats(user));
    }

    /**
     * Mark a single notification as read
     */
    @org.springframework.web.bind.annotation.PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    /**
     * Mark all notifications as read
     */
    @org.springframework.web.bind.annotation.PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        // Mark all as read
        int updatedCount = notificationService.markAllAsRead(user);
        
        // Get current unread count (should be 0 after marking all as read)
        long unreadCount = notificationService.getUnreadCount(user);
        
        return ResponseEntity.ok(Map.of(
            "message", "All notifications marked as read",
            "updatedCount", updatedCount,
            "unreadCount", unreadCount
        ));
    }

    /**
     * Delete a notification
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        notificationService.deleteNotification(id, user);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    /**
     * Clear all read notifications
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/clear-read")
    public ResponseEntity<Map<String, String>> clearReadNotifications(Authentication auth) {
        User user = (User) auth.getPrincipal();
        int count = notificationService.clearReadNotifications(user);
        return ResponseEntity.ok(Map.of(
            "message", "Read notifications cleared",
            "count", String.valueOf(count)
        ));
    }
}
