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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;
   
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    private RoleName targetRole;         // CITIZEN / WARD_OFFICER / DEPT_OFFICER / ADMIN
    
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
    
    @Column(nullable = false)
    private boolean seen = false;
    
    private Long referenceId; // complaintId
    
    @PrePersist
    public void prePersist() {
    	if (createdAt == null) createdAt = LocalDateTime.now();
    }
}