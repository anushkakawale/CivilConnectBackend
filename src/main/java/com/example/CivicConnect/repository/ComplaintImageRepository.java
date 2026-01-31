package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintImage;
import com.example.CivicConnect.entity.enums.ImageStage;

public interface ComplaintImageRepository
        extends JpaRepository<ComplaintImage, Long> {

    // 🔹 All images for a complaint (ordered by upload time)
    List<ComplaintImage> findByComplaintOrderByUploadedAtAsc(Complaint complaint);
    
    // 🔹 Find by complaint ID
    List<ComplaintImage> findByComplaint_ComplaintId(Long complaintId);
    
    // 🔹 Find by complaint ID (ordered by upload time)
    List<ComplaintImage> findByComplaint_ComplaintIdOrderByUploadedAtAsc(Long complaintId);

    // 🔹 Filter by image stage
    List<ComplaintImage> findByComplaintAndImageStageOrderByUploadedAtAsc(
            Complaint complaint,
            ImageStage imageStage
    );
    
    // 🔹 Filter by complaint ID and stage
    List<ComplaintImage> findByComplaint_ComplaintIdAndImageStageOrderByUploadedAtAsc(
            Long complaintId,
            ImageStage imageStage
    );
    
    // 🔹 Count images by complaint
    long countByComplaint(Complaint complaint);
}
