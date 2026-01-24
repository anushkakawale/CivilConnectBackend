package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintImage;
import com.example.CivicConnect.entity.enums.UploadedBy;

public interface ComplaintImageRepository
        extends JpaRepository<ComplaintImage, Long> {

    // 🔹 All images for a complaint
    List<ComplaintImage> findByComplaint(Complaint complaint);

    // 🔹 Filter by uploadedBy
    List<ComplaintImage> findByComplaintAndUploadedBy(
            Complaint complaint,
            UploadedBy uploadedBy
    );

    // 🔹 Filter by multiple uploaders
    List<ComplaintImage> findByComplaintAndUploadedByIn(
            Complaint complaint,
            List<UploadedBy> uploadedBy
    );
    List<ComplaintImage> findByComplaintOrderByUploadedAtAsc(
            Complaint complaint
    );
}
