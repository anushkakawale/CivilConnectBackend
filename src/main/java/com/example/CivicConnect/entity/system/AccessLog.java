package com.example.CivicConnect.entity.system;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.core.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "access_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    private User userId;

    private String action;
    
    private String entityType;
    
    private Long entityId;
    
    private String ipAddress;

    private LocalDateTime createdAt = LocalDateTime.now();
}