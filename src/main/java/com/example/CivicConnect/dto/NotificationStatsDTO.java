package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification statistics displayed on dashboards
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStatsDTO {
    private Long totalNotifications;
    private Long unreadCount;
    private Long unseenCount;
}
