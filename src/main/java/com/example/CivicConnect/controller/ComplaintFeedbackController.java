package com.example.CivicConnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.CivicConnect.dto.ComplaintFeedbackDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.ComplaintFeedbackService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class ComplaintFeedbackController {

    private final ComplaintFeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<?> submitFeedback(
            @Valid @RequestBody ComplaintFeedbackDTO dto,
            Authentication auth) {
        
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(feedbackService.submitFeedback(dto, citizen));
    }

    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<List<Map<String, Object>>> getComplaintFeedback(
            @PathVariable Long complaintId) {
        return ResponseEntity.ok(feedbackService.getComplaintFeedback(complaintId));
    }

    @GetMapping("/ward/{wardId}")
    public ResponseEntity<List<Map<String, Object>>> getWardFeedback(
            @PathVariable Long wardId) {
        return ResponseEntity.ok(feedbackService.getWardFeedback(wardId));
    }

    @GetMapping("/ward/{wardId}/stats")
    public ResponseEntity<Map<String, Object>> getWardFeedbackStats(
            @PathVariable Long wardId) {
        return ResponseEntity.ok(feedbackService.getWardFeedbackStats(wardId));
    }

    @GetMapping("/department/{departmentId}/stats")
    public ResponseEntity<Map<String, Object>> getDepartmentFeedbackStats(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(feedbackService.getDepartmentFeedbackStats(departmentId));
    }

    @GetMapping("/can-provide/{complaintId}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Map<String, Boolean>> canProvideFeedback(
            @PathVariable Long complaintId,
            Authentication auth) {
        
        User citizen = (User) auth.getPrincipal();
        boolean canProvide = feedbackService.canProvideFeedback(complaintId, citizen.getUserId());
        return ResponseEntity.ok(Map.of("canProvideFeedback", canProvide));
    }
}
