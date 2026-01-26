package com.example.CivicConnect.entity.sla;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.SLAStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "complaint_sla")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintSla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slaId;

    @OneToOne(optional = false)
    @JoinColumn(name = "complaint_id", unique = true)
    private Complaint complaint;

    @Column(nullable = false)
    private LocalDateTime slaStartTime;

    @Column(nullable = false)
    private LocalDateTime slaDeadline;

    private LocalDateTime slaEndTime;
    
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private boolean escalated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SLAStatus status;
}