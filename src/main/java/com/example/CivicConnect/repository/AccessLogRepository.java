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
    
    Page<AccessLog> findByActionAndEntityTypeAndUserId_UserIdOrderByCreatedAtDesc(
            String action, String entityType, Long userId, Pageable pageable);
    
    List<AccessLog> findByCreatedAtAfter(LocalDateTime dateTime);
}
