package com.example.CivicConnect.service.admincomplaint;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            logs = accessLogRepository.findByActionAndEntityTypeAndUser_UserIdOrderByCreatedAtDesc(
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

        List<AccessLog> logs =
                accessLogRepository.findByCreatedAtAfter(last24Hours);

        Map<String, Object> summary = new HashMap<>();

        summary.put("totalLogs24Hours", logs.size());

        summary.put("actions",
                logs.stream()
                    .collect(Collectors.groupingBy(
                            AccessLog::getAction,
                            Collectors.counting()
                    )));

        summary.put("entities",
                logs.stream()
                    .collect(Collectors.groupingBy(
                            AccessLog::getEntityType,
                            Collectors.counting()
                    )));

        return summary;
    }


    private AuditLogDTO toAuditLogDTO(AccessLog log) {
        return new AuditLogDTO(
                log.getLogId(),
                log.getUser() != null ? log.getUser().getUserId() : null,
                log.getUser() != null ? log.getUser().getName() : null,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }
}
