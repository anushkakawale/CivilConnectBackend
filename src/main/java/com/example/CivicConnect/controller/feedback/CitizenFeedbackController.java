package com.example.CivicConnect.controller.feedback;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.CitizenFeedbackRequestDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.CitizenFeedbackRepository;
import com.example.CivicConnect.service.feedback.CitizenFeedbackService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizens/feedback")
@RequiredArgsConstructor
public class CitizenFeedbackController {

    private final CitizenFeedbackService service;
    private final CitizenFeedbackRepository feedbackRepository;

    @PostMapping("/{complaintId}/{citizenUserId}")
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long complaintId,
            @PathVariable Long citizenUserId,
            @Valid @RequestBody CitizenFeedbackRequestDTO dto) {

        service.submitFeedback(complaintId, citizenUserId, dto);
        return ResponseEntity.ok("Feedback submitted successfully");
    }
    
    @GetMapping("/{complaintId}/feedback/status")
    public ResponseEntity<?> feedbackStatus(
            @PathVariable Long complaintId,
            Authentication auth) {

        User citizen = (User) auth.getPrincipal();

        boolean submitted =
            feedbackRepository
                .existsByComplaint_ComplaintIdAndCitizen_UserId(
                    complaintId,
                    citizen.getUserId()
                );

        return ResponseEntity.ok(
            Map.of("submitted", submitted)
        );
    }
}