package com.example.CivicConnect.controller.citizencomplaint;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.CivicConnect.dto.ComplaintRequestDTO;
import com.example.CivicConnect.dto.ComplaintResponseDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.Priority;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.service.citizen.CitizenComplaintListService;
import com.example.CivicConnect.service.citizencomplaint.ComplaintService;

import com.example.CivicConnect.service.SharedComplaintService;

import lombok.RequiredArgsConstructor;

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
    private final SharedComplaintService sharedComplaintService;
    private final UserRepository userRepository;
    private final com.example.CivicConnect.repository.ComplaintSlaRepository slaRepository;

    // 1️⃣ REGISTER COMPLAINT (JSON Version)
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ComplaintResponseDTO> registerComplaint(
            @RequestBody ComplaintRequestDTO request,
            Authentication auth) {

        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                complaintService.registerComplaint(request, citizen)
        );
    }

    // 1️⃣.1️⃣ REGISTER COMPLAINT (Multipart/FormData Version for Images)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ComplaintResponseDTO> registerComplaintMultipart(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("departmentId") Long departmentId,
            @RequestParam(value = "priority", defaultValue = "MEDIUM") Priority priority,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            Authentication auth) {

        ComplaintRequestDTO request = new ComplaintRequestDTO();
        request.setTitle(title);
        request.setDescription(description);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setDepartmentId(departmentId);
        request.setPriority(priority);

        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                complaintService.registerComplaintWithImages(request, citizen, images)
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
        var details = sharedComplaintService.getComplaintDetails(id);
        
        // Security: Citizen can view any complaint's basic info, but usually they track their own
        // If we want strict restriction:
        // if (!details.getCitizenName().equals(citizen.getName()) && ...) 
        
        return ResponseEntity.ok(details);
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
