package com.example.CivicConnect.entity.profiles;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ward_change_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @ManyToOne
    @JoinColumn(name = "old_ward_id")
    private Ward oldWard;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_ward_id", nullable = false)
    private Ward requestedWard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;
    
    private LocalDateTime decidedAt;
    
    @ManyToOne
    @JoinColumn(name = "decided_by")
    private User decidedBy; // Ward Officer who approved/rejected
    
    @Column(length = 500)
    private String remarks;
    
    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ApprovalStatus.PENDING;
        }
    }
}
