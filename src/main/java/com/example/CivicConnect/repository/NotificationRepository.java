package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.Notification;

public interface NotificationRepository 
extends JpaRepository<Notification, Long>
{
	// All notifications of a user (latest first)
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // Unread notifications
    List<Notification> findByUser_UserIdAndSeenFalse(Long userId);

    List<Notification> findByUserAndSeenFalse(User user);
    
 // 🔔 Unread count
    long countByUserAndIsReadFalse(User user);

    // 🔔 Fetch unread notifications
    List<Notification> findByUserAndIsReadFalse(User user);

    // 🔔 Fetch all notifications for user (optional but recommended)
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
