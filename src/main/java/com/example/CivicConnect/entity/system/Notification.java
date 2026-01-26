package com.example.CivicConnect.entity.system;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.RoleName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    private User user;
    
    private String title;

    private String message;
   

    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    RoleName targetRole;         // CITIZEN / WARD_OFFICER / DEPT_OFFICER / ADMIN
    
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    
    private Long referenceId; // complaintId





}