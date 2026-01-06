package com.example.CivicConnect.entity.complaint;

import com.example.CivicConnect.entity.enums.UploadedBy;

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
@Table(name = "complaint_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private UploadedBy uploadedBy;

    @ManyToOne
    private Complaint complaint;
}