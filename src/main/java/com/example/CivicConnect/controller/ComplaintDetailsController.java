package com.example.CivicConnect.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintImage;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.core.feedback.CitizenFeedback;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.CitizenFeedbackRepository;
import com.example.CivicConnect.repository.ComplaintImageRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintDetailsController {

    private final ComplaintRepository complaintRepository;
    private final ComplaintImageRepository imageRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final CitizenFeedbackRepository feedbackRepository;
    private final NotificationService notificationService;

    /**
     * Get complete complaint details with images, timeline, and feedback
     */
    @GetMapping("/{complaintId}/details")
    public ResponseEntity<?> getComplaintDetails(@PathVariable Long complaintId) {
        
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Get all images
        List<ComplaintImage> images = imageRepository.findByComplaint_ComplaintId(complaintId);
        List<Map<String, Object>> imageList = images.stream()
                .map(img -> Map.<String, Object>of(
                        "id", img.getImageId(),
                        "imageUrl", "/api/images/" + img.getImageUrl(),
                        "imageStage", img.getImageStage().name(),
                        "uploadedBy", img.getUploadedBy().getName(),
                        "uploadedByRole", img.getUploadedBy().getRole().name(),
                        "uploadedAt", img.getUploadedAt(),
                        "latitude", img.getLatitude(),
                        "longitude", img.getLongitude()
                ))
                .collect(Collectors.toList());

        // Get status history (timeline)
        List<ComplaintStatusHistory> history = statusHistoryRepository
                .findByComplaint_ComplaintIdOrderByChangedAtDesc(complaintId);
        List<Map<String, Object>> timeline = history.stream()
                .map(h -> Map.<String, Object>of(
                        "status", h.getStatus().name(),
                        "changedAt", h.getChangedAt(),
                        "changedBy", h.getChangedBy() != null ? h.getChangedBy().getName() : "System",
                        "remarks", h.getRemarks() != null ? h.getRemarks() : ""
                ))
                .collect(Collectors.toList());

        // Get feedback if exists
        CitizenFeedback feedback = feedbackRepository
                .findByComplaint_ComplaintId(complaintId)
                .orElse(null);

        Map<String, Object> feedbackData = null;
        if (feedback != null) {
            feedbackData = Map.<String, Object>of(
                    "rating", feedback.getRating(),
                    "comment", feedback.getComment() != null ? feedback.getComment() : "",
                    "submittedAt", feedback.getCreatedAt()
            );
        }

        // Check if can reopen (within 7 days of closure)
        boolean canReopen = false;
        if (complaint.getStatus() == ComplaintStatus.CLOSED) {
            LocalDateTime closedAt = complaint.getUpdatedAt();
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            canReopen = closedAt.isAfter(sevenDaysAgo);
        }

        // Build complete response
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("complaintId", complaint.getComplaintId());
        response.put("title", complaint.getTitle());
        response.put("description", complaint.getDescription());
        response.put("status", complaint.getStatus().name());
        response.put("priority", complaint.getPriority().name());
        response.put("wardName", complaint.getWard().getAreaName());
        response.put("departmentName", complaint.getDepartment().getName());
        response.put("citizenName", complaint.getCitizen().getName());
        response.put("citizenEmail", complaint.getCitizen().getEmail());
        response.put("assignedOfficer", complaint.getAssignedOfficer() != null 
                ? complaint.getAssignedOfficer().getName() : "Not Assigned");
        response.put("createdAt", complaint.getCreatedAt());
        response.put("updatedAt", complaint.getUpdatedAt());
        response.put("latitude", complaint.getLatitude());
        response.put("longitude", complaint.getLongitude());
        response.put("images", imageList);
        response.put("timeline", timeline);
        response.put("feedback", feedbackData);
        response.put("canReopen", canReopen);
        response.put("slaDeadline", complaint.getSla() != null ? complaint.getSla().getSlaDeadline() : null);
        response.put("slaStatus", complaint.getSla() != null ? complaint.getSla().getStatus().name() : null);

        return ResponseEntity.ok(response);
    }

    /**
     * Reopen a closed complaint (within 7 days)
     */
    @PutMapping("/{complaintId}/reopen")
    public ResponseEntity<?> reopenComplaint(
            @PathVariable Long complaintId,
            @RequestBody Map<String, String> request,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Validate: Must be citizen who created it
        if (!complaint.getCitizen().getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(403).body(Map.<String, Object>of("error", "Only the complaint creator can reopen"));
        }

        // Validate: Must be closed
        if (complaint.getStatus() != ComplaintStatus.CLOSED) {
            return ResponseEntity.badRequest().body(Map.<String, Object>of("error", "Only closed complaints can be reopened"));
        }

        // Validate: Within 7 days
        LocalDateTime closedAt = complaint.getUpdatedAt();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        if (closedAt.isBefore(sevenDaysAgo)) {
            return ResponseEntity.badRequest().body(Map.<String, Object>of("error", "Complaint can only be reopened within 7 days of closure"));
        }

        // Reopen complaint
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        // Add to history
        ComplaintStatusHistory historyEntry = ComplaintStatusHistory.builder()
                .complaint(complaint)
                .status(ComplaintStatus.IN_PROGRESS)
                .changedBy(user)
                .changedAt(LocalDateTime.now())
                .remarks("Reopened by citizen: " + request.getOrDefault("reason", "Not satisfied with resolution"))
                .build();
        statusHistoryRepository.save(historyEntry);

        // Notify assigned officer
        if (complaint.getAssignedOfficer() != null) {
            notificationService.notifyOfficer(
                    complaint.getAssignedOfficer(),
                    "Complaint Reopened",
                    "Complaint #" + complaintId + " has been reopened by the citizen",
                    complaintId,
                    com.example.CivicConnect.entity.enums.NotificationType.REOPENED
            );
        }

        return ResponseEntity.ok(Map.<String, Object>of(
                "message", "Complaint reopened successfully",
                "complaintId", complaintId,
                "status", "IN_PROGRESS"
        ));
    }

    /**
     * Submit feedback for a closed complaint
     */
    @PostMapping("/{complaintId}/feedback")
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long complaintId,
            @RequestBody Map<String, Object> request,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Validate: Must be citizen who created it
        if (!complaint.getCitizen().getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(403).body(Map.<String, Object>of("error", "Only the complaint creator can give feedback"));
        }

        // Validate: Must be closed
        if (complaint.getStatus() != ComplaintStatus.CLOSED) {
            return ResponseEntity.badRequest().body(Map.<String, Object>of("error", "Feedback can only be given for closed complaints"));
        }

        // Check if feedback already exists
        if (feedbackRepository.findByComplaint_ComplaintId(complaintId).isPresent()) {
            return ResponseEntity.badRequest().body(Map.<String, Object>of("error", "Feedback already submitted"));
        }

        // Create feedback
        Integer rating = (Integer) request.get("rating");
        String comment = (String) request.getOrDefault("comment", "");

        if (rating == null || rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.<String, Object>of("error", "Rating must be between 1 and 5"));
        }

        CitizenFeedback feedback = CitizenFeedback.builder()
                .complaint(complaint)
                .citizen(complaint.getCitizen())
                .rating(rating)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        feedbackRepository.save(feedback);

        return ResponseEntity.ok(Map.<String, Object>of(
                "message", "Feedback submitted successfully",
                "rating", rating
        ));
    }
}
