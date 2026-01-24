package com.example.CivicConnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.feedback.CitizenFeedback;

public interface CitizenFeedbackRepository
extends JpaRepository<CitizenFeedback, Long> {

boolean existsByComplaint_ComplaintId(Long complaintId);
boolean existsByComplaint(Complaint complaint);
Optional<CitizenFeedback> findByComplaint_ComplaintId(Long complaintId);
}