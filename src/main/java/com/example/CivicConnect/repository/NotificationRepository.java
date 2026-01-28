package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.Notification;

public interface NotificationRepository 
extends JpaRepository<Notification, Long>
{
	// All notifications of a user (latest first)
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    
    // Paginated notifications
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 🔔 Unread count
    long countByUserAndIsReadFalse(User user);
    
    // 🔔 Unseen count
    long countByUserAndSeenFalse(User user);

    // 🔔 Fetch unread notifications
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    // 🔔 Fetch all notifications for user (optional but recommended)
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // 🔔 Mark all as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    int markAllAsRead(@Param("user") User user);
    
    // 🔔 Mark all as seen
    @Modifying
    @Query("UPDATE Notification n SET n.seen = true WHERE n.user = :user AND n.seen = false")
    int markAllAsSeen(@Param("user") User user);
}
