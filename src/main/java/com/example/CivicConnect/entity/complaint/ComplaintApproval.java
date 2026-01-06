package com.example.CivicConnect.entity.complaint;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.RoleName;

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
@Table(name = "complaint_approvals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalId;

    @ManyToOne
    private Complaint complaint;

    @ManyToOne
    private User approvedBy;

    @Enumerated(EnumType.STRING)
    private RoleName roleAtTime;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    private String remarks;
    private LocalDateTime approvedAt;
}