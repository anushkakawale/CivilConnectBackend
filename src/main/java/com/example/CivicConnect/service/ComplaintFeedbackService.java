package com.example.CivicConnect.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.ComplaintFeedbackDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintFeedback;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintFeedbackRepository;
import com.example.CivicConnect.repository.ComplaintRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintFeedbackService {

    private final ComplaintFeedbackRepository feedbackRepository;
    private final ComplaintRepository complaintRepository;

    public Map<String, Object> submitFeedback(ComplaintFeedbackDTO dto, User citizen) {
        Complaint complaint = complaintRepository.findById(dto.getComplaintId())
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Verify citizen owns this complaint
        if (!complaint.getCitizen().getUserId().equals(citizen.getUserId())) {
            throw new RuntimeException("You can only provide feedback for your own complaints");
        }

        // Verify complaint is closed or approved
        if (complaint.getStatus() != ComplaintStatus.CLOSED && 
            complaint.getStatus() != ComplaintStatus.APPROVED &&
            complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new RuntimeException("Feedback can only be submitted for resolved, closed or approved complaints");
        }

        // Check if feedback already exists
        if (feedbackRepository.existsByComplaint_ComplaintIdAndCitizen_UserId(
                dto.getComplaintId(), citizen.getUserId())) {
            throw new RuntimeException("You have already submitted feedback for this complaint");
        }

        ComplaintFeedback feedback = ComplaintFeedback.builder()
                .complaint(complaint)
                .citizen(citizen)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        ComplaintFeedback saved = feedbackRepository.save(feedback);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Feedback submitted successfully");
        response.put("feedbackId", saved.getFeedbackId());
        response.put("rating", saved.getRating());
        return response;
    }

    public List<Map<String, Object>> getComplaintFeedback(Long complaintId) {
        return feedbackRepository.findByComplaint_ComplaintIdOrderByCreatedAtDesc(complaintId)
                .stream()
                .map(f -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("feedbackId", f.getFeedbackId());
                    map.put("citizenName", f.getCitizen() != null ? f.getCitizen().getName() : "Anonymous");
                    map.put("rating", f.getRating());
                    map.put("comment", f.getComment() != null ? f.getComment() : "");
                    map.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : "N/A");
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getWardFeedback(Long wardId) {
        return feedbackRepository.findByComplaint_Ward_WardIdOrderByCreatedAtDesc(wardId)
                .stream()
                .map(f -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("feedbackId", f.getFeedbackId());
                    map.put("complaintId", f.getComplaint().getComplaintId());
                    map.put("complaintTitle", f.getComplaint().getTitle());
                    map.put("citizenName", f.getCitizen() != null ? f.getCitizen().getName() : "Anonymous");
                    map.put("rating", f.getRating());
                    map.put("comment", f.getComment() != null ? f.getComment() : "");
                    map.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : "N/A");
                    return map;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getWardFeedbackStats(Long wardId) {
        Double avgRating = feedbackRepository.getAverageRatingByWard(wardId);
        long totalFeedback = feedbackRepository.countByComplaint_Ward_WardId(wardId);

        return Map.of(
            "averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
            "totalFeedback", totalFeedback,
            "wardId", wardId
        );
    }

    public Map<String, Object> getDepartmentFeedbackStats(Long departmentId) {
        Double avgRating = feedbackRepository.getAverageRatingByDepartment(departmentId);
        long totalFeedback = feedbackRepository.countByComplaint_Department_DepartmentId(departmentId);

        return Map.of(
            "averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
            "totalFeedback", totalFeedback,
            "departmentId", departmentId
        );
    }

    public boolean canProvideFeedback(Long complaintId, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Check if user owns the complaint
        if (!complaint.getCitizen().getUserId().equals(userId)) {
            return false;
        }

        // Check if complaint is in eligible status
        if (complaint.getStatus() != ComplaintStatus.CLOSED && 
            complaint.getStatus() != ComplaintStatus.APPROVED &&
            complaint.getStatus() != ComplaintStatus.RESOLVED) {
            return false;
        }

        // Check if feedback already exists
        return !feedbackRepository.existsByComplaint_ComplaintIdAndCitizen_UserId(complaintId, userId);
    }
}
