package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Long referenceId;
    private LocalDateTime createdAt;
    private boolean isRead;
    private String timeElapsed; // "2 min ago"
}