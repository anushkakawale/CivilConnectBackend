package com.example.CivicConnect.entity.complaint;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "escalation_log")
public class EscalationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long escalationId;

    private Long complaintId;

    private String fromRole;
    private String toRole;

    private String reason;

    private LocalDateTime escalatedAt;

}
