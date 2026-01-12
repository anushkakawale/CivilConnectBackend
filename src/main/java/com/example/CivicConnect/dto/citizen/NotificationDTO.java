package com.example.CivicConnect.dto.citizen;

import lombok.Data;

@Data
public class NotificationDTO {
    private Long id;
    private String message;
    private Boolean seen;
    private String createdAt;
}

