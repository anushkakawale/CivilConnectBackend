package com.example.CivicConnect.entity.complaint;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.ComplaintStatus;

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
@Table(name = "complaint_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    private String changedBy;
    private LocalDateTime changedAt = LocalDateTime.now();
}