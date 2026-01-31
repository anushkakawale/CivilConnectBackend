package com.example.CivicConnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.NotificationStats;

public interface NotificationStatsRepository extends JpaRepository<NotificationStats, Long> {
    
    /**
     * Find notification stats for a specific user
     */
    Optional<NotificationStats> findByUser(User user);
    
    /**
     * Find notification stats by user ID
     */
    Optional<NotificationStats> findByUser_UserId(Long userId);
    
    /**
     * Check if stats exist for a user
     */
    boolean existsByUser(User user);
}
