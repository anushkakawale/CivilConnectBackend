package com.example.CivicConnect.entity.core.feedback;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "citizen_feedback")
@Data
public class CitizenFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @OneToOne
    @JoinColumn(name = "complaint_id", nullable = false, unique = true)
    private Complaint complaint;

    @ManyToOne
    @JoinColumn(name = "citizen_user_id", nullable = false)
    private User citizen;

    private int rating; // 1 to 5

    @Column(length = 500)
    private String comments;

    private LocalDateTime createdAt;
}
