package com.example.CivicConnect.service.system;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.AccessLog;
import com.example.CivicConnect.repository.AccessLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository repository;

    public void log(
            User user,
            String action,
            String entityType,
            Long entityId,
            String ipAddress) {

        AccessLog log = new AccessLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setIpAddress(ipAddress);

        repository.save(log);
    }
}
