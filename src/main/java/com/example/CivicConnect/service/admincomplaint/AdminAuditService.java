package com.example.CivicConnect.service.admincomplaint;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.AuditLogDTO;
import com.example.CivicConnect.entity.system.AccessLog;
import com.example.CivicConnect.repository.AccessLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuditService {

    private final AccessLogRepository accessLogRepository;

    public Page<AuditLogDTO> getAuditLogs(
            String action,
            String entityType,
            Long userId,
            Pageable pageable) {

        Page<AccessLog> logs;
        
        if (action != null && entityType != null && userId != null) {
            logs = accessLogRepository.findByActionAndEntityTypeAndUserId_UserIdOrderByCreatedAtDesc(
                    action, entityType, userId, pageable);
        } else if (action != null && entityType != null) {
            logs = accessLogRepository.findByActionAndEntityTypeOrderByCreatedAtDesc(
                    action, entityType, pageable);
        } else if (action != null) {
            logs = accessLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
        } else if (entityType != null) {
            logs = accessLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable);
        } else {
            logs = accessLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return logs.map(this::toAuditLogDTO);
    }

    public Map<String, Object> getAuditSummary() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        
        List<AccessLog> recentLogs = accessLogRepository.findByCreatedAtAfter(last24Hours);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLogs24Hours", recentLogs.size());
        
        // Count by action type
        Map<String, Long> actionCounts = new HashMap<>();
        recentLogs.forEach(log -> 
            actionCounts.merge(log.getAction(), 1L, Long::sum));
        summary.put("actionCounts", actionCounts);
        
        // Count by entity type
        Map<String, Long> entityCounts = new HashMap<>();
        recentLogs.forEach(log -> 
            entityCounts.merge(log.getEntityType(), 1L, Long::sum));
        summary.put("entityCounts", entityCounts);
        
        return summary;
    }

    private AuditLogDTO toAuditLogDTO(AccessLog log) {
        return new AuditLogDTO(
                log.getLogId(),
                log.getUserId() != null ? log.getUserId().getUserId() : null,
                log.getUserId() != null ? log.getUserId().getName() : null,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }
}
