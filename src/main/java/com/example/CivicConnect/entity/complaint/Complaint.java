package com.example.CivicConnect.entity.complaint;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.geography.Department;
import com.example.CivicConnect.entity.geography.Ward;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complaintId;

    private String title;
    
    @Column(length=2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status; //= ComplaintStatus.SUBMITTED;

    private int duplicateCount = 0;

    private Double latitude;
    private Double longitude;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    
 //  TRACEABILITY
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "last_updated_by_user_id")
    private User lastUpdatedBy;

    @ManyToOne
    @JoinColumn(name = "closed_by_admin_id")
    private User closedByAdmin;

    private LocalDateTime closedAt;

    //  SLA
    private LocalDateTime slaDeadline;
    
    @Column(nullable = false)
    private boolean slaBreached = false;
    
    @Column(nullable = false)
    private boolean escalated = false;

    //  RELATIONS
    
    @ManyToOne
    @JoinColumn(name = "citizen_user_id", nullable = false)
    private User citizen;

    @ManyToOne
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;
}