package com.example.CivicConnect.entity.complaint;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.UploadedBy;

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
@Table(name = "complaint_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Column(nullable = false)
    private String imageUrl;   // Cloud URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadedBy uploadedBy; // CITIZEN / DEPARTMENT_OFFICER / WARD_OFFICER

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private LocalDateTime uploadedAt;
}
