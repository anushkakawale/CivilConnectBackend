package com.example.CivicConnect.service.feedback;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.CitizenFeedbackRequestDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.feedback.CitizenFeedback;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.CitizenFeedbackRepository;
import com.example.CivicConnect.repository.ComplaintRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CitizenFeedbackService {

    private final ComplaintRepository complaintRepository;
    private final CitizenFeedbackRepository feedbackRepository;

    public void submitFeedback(
            Long complaintId,
            Long citizenUserId,
            CitizenFeedbackRequestDTO dto) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!complaint.getCitizen().getUserId().equals(citizenUserId)) {
            throw new RuntimeException("Access denied");
        }

        if (complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new RuntimeException("Feedback allowed only after closure");
        }

        if (feedbackRepository.existsByComplaint_ComplaintId(complaintId)) {
            throw new RuntimeException("Feedback already submitted");
        }

        CitizenFeedback feedback = new CitizenFeedback();
        feedback.setComplaint(complaint);
        feedback.setCitizen(complaint.getCitizen());
        feedback.setRating(dto.getRating());
        feedback.setComment(dto.getComment());
        feedback.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);
    }
}
