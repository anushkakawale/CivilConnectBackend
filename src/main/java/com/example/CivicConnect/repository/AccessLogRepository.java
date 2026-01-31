package com.example.CivicConnect.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.system.AccessLog;

public interface AccessLogRepository
        extends JpaRepository<AccessLog, Long> {
    
    Page<AccessLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<AccessLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    Page<AccessLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);
    
    Page<AccessLog> findByActionAndEntityTypeOrderByCreatedAtDesc(
            String action, String entityType, Pageable pageable);
    
    Page<AccessLog> findByActionAndEntityTypeAndUser_UserIdOrderByCreatedAtDesc(
            String action, String entityType, Long userId, Pageable pageable);
    
    List<AccessLog> findByCreatedAtAfter(LocalDateTime dateTime);
    
    Page<AccessLog> findByActionAndEntityTypeAndUser_UserId(
            String action, String entityType, Long userId, Pageable pageable);

    Page<AccessLog> findByActionAndEntityType(
            String action, String entityType, Pageable pageable);

    Page<AccessLog> findByUser_UserId(Long userId, Pageable pageable);

    long countByCreatedAtAfter(LocalDateTime time);

    long countByEntityType(String entityType);

    long countByAction(String action);

    long countByActionContaining(String keyword);

}
