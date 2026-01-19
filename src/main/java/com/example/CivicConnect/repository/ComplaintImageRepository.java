package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.ComplaintImage;

public interface ComplaintImageRepository
extends JpaRepository<ComplaintImage, Long> {

List<ComplaintImage> findByComplaint_ComplaintId(Long complaintId);
}
