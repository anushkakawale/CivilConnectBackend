package com.example.CivicConnect.controller.citizencomplaint;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

/**
 * Unified Controller for Citizen Complaint Operations
 * Simplified to use plural path /api/citizens/complaints to match frontend
 */
@RestController
@RequestMapping("/api/citizens/complaints")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CITIZEN')")
public class CitizenComplaintController {

    private final CitizenComplaintListService citizenComplaintListService;
    private final ComplaintService complaintService;
    private final UserRepository userRepository;
    private final com.example.CivicConnect.repository.ComplaintSlaRepository slaRepository;

    // 1️⃣ REGISTER COMPLAINT (Moved from ComplaintController)
    @PostMapping
    public ResponseEntity<ComplaintResponseDTO> registerComplaint(
            @RequestBody ComplaintRequestDTO request,
            Authentication auth) {

        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                complaintService.registerComplaint(request, citizen)
        );
    }

    // 2️⃣ VIEW MY COMPLAINTS (Paginated with filters)
    @GetMapping
    public ResponseEntity<?> complaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) SLAStatus slaStatus,
            Authentication auth
    ) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                citizenComplaintListService.getMyComplaints(
                        citizen, page, size, status, priority, slaStatus
                )
        );
    }

    // 3️⃣ VIEW WARD COMPLAINTS
    @GetMapping("/ward")
    public ResponseEntity<?> wardComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth
    ) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(citizenComplaintListService.getWardComplaints(citizen, page, size));
    }

    // 4️⃣ VIEW TIMELINE/TRACKING (Unified)
    @GetMapping("/{id}")
    public ResponseEntity<?> trackComplaint(@PathVariable Long id, Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(complaintService.trackComplaint(id, citizen.getUserId()));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<?> timeline(@PathVariable Long id) {
        return ResponseEntity.ok(citizenComplaintListService.getTimeline(id));
    }

    // 🔍 5️⃣ VIEW SLA DETAILS
    @GetMapping("/{complaintId}/sla")
    public ResponseEntity<?> mySla(@PathVariable Long complaintId, Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
            slaRepository.findByComplaint_ComplaintIdAndComplaint_Citizen_UserId(
                    complaintId, citizen.getUserId()
            ).orElseThrow(() -> new RuntimeException("SLA not found"))
        );
    }

    // ⏱ 6️⃣ SLA COUNTDOWN
    @GetMapping("/{complaintId}/sla/countdown")
    public ResponseEntity<?> slaCountdown(@PathVariable Long complaintId, Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        var sla = slaRepository.findByComplaint_ComplaintIdAndComplaint_Citizen_UserId(
                complaintId, citizen.getUserId()
        ).orElseThrow(() -> new RuntimeException("SLA not found"));

        long remainingMinutes = java.time.Duration.between(java.time.LocalDateTime.now(), sla.getSlaDeadline()).toMinutes();

        return ResponseEntity.ok(Map.of(
                "deadline", sla.getSlaDeadline(),
                "remainingMinutes", remainingMinutes,
                "breached", remainingMinutes < 0
        ));
    }

    // 7️⃣ REOPEN COMPLAINT
    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopenComplaint(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication auth) {

        User citizen = (User) auth.getPrincipal();
        String remarks = request.get("remarks");
        complaintService.reopenComplaint(id, citizen.getUserId(), remarks);
        
        return ResponseEntity.ok(Map.of("message", "Complaint reopened successfully"));
    }
    // 8️⃣ SUBMIT FEEDBACK
    @PutMapping("/{id}/feedback")
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication auth) {

        User citizen = (User) auth.getPrincipal();
        Integer rating = (Integer) request.get("rating");
        String comments = (String) request.get("comments");
        
        complaintService.submitFeedback(id, citizen.getUserId(), rating, comments);
        
        return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully"));
    }

}
