package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long logId;
    private Long userId;
    private String userName;
    private String action;
    private String entityType;
    private Long entityId;
    private String ipAddress;
    private LocalDateTime createdAt;
}
