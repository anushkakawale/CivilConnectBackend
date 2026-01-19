package com.example.CivicConnect.entity.complaint;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.core.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "complaint_updates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintUpdate {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long updateId;

    @ManyToOne
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne
    @JoinColumn(name = "officer_user_id", nullable = false)
    private User officer;

    @Column(length = 500)
    private String message;
    
    private LocalDateTime updatedAt = LocalDateTime.now();
}