package com.example.CivicConnect.controller.feedback;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.CitizenFeedbackRequestDTO;
import com.example.CivicConnect.service.feedback.CitizenFeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/citizen/feedback")
public class CitizenFeedbackController {

    private final CitizenFeedbackService service;

    public CitizenFeedbackController(CitizenFeedbackService service) {
        this.service = service;
    }

    @PostMapping("/{complaintId}/{citizenUserId}")
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long complaintId,
            @PathVariable Long citizenUserId,
            @Valid @RequestBody CitizenFeedbackRequestDTO dto) {

        service.submitFeedback(complaintId, citizenUserId, dto);
        return ResponseEntity.ok("Feedback submitted successfully");
    }
}