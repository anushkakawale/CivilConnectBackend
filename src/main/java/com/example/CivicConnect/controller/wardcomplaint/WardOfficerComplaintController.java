package com.example.CivicConnect.controller.wardcomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintDecisionDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.wardcomplaint.WardOfficerComplaintService;

@RestController
@RequestMapping("/api/ward-officer/complaints")
@PreAuthorize("hasRole('WARD_OFFICER')")
public class WardOfficerComplaintController {

    private final WardOfficerComplaintService service;
    private final com.example.CivicConnect.repository.OfficerProfileRepository officerProfileRepository;
    private final com.example.CivicConnect.service.SharedComplaintService sharedService;

    public WardOfficerComplaintController(
            WardOfficerComplaintService service,
            com.example.CivicConnect.repository.OfficerProfileRepository officerProfileRepository,
            com.example.CivicConnect.service.SharedComplaintService sharedService) {
        this.service = service;
        this.officerProfileRepository = officerProfileRepository;
        this.sharedService = sharedService;
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<?> getDetails(@PathVariable Long complaintId) {
        // Shared details for any complaint reachable by this role
        return ResponseEntity.ok(sharedService.getComplaintDetails(complaintId));
    }

 // ✅ APPROVE COMPLAINT
    @PutMapping("/{complaintId}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long complaintId,
            @RequestBody ComplaintDecisionDTO dto,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal();
        service.approve(complaintId, officer, dto.getRemarks());
        return ResponseEntity.ok("Complaint APPROVED");
    }

    @PutMapping("/{complaintId}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long complaintId,
            @RequestBody ComplaintDecisionDTO dto,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal();
        service.reject(complaintId, officer, dto.getRemarks());
        return ResponseEntity.ok("Complaint REJECTED");
    }

    // ✅ ASSIGN OFFICER
    @PutMapping("/{complaintId}/assign")
    public ResponseEntity<?> assign(
            @PathVariable Long complaintId,
            @RequestBody java.util.Map<String, Long> request,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal(); // Ward Officer
        Long targetOfficerId = request.get("officerId");
        
        service.assignOfficer(complaintId, targetOfficerId, officer);
        return ResponseEntity.ok("Complaint ASSIGNED to new officer");
    }

    @GetMapping
    public ResponseEntity<?> getAllComplaints(
            org.springframework.data.domain.Pageable pageable,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal();
        // Assuming you have access to officerProfileRepository or can get wardId from User attributes if stored
        // For now, let's fetch it from the service or repository.
        // Actually, the service needs the wardId.
        
        return ResponseEntity.ok(java.util.Map.of("message", "Use /api/ward-officer/complaints/all for paginated list"));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getWardComplaints(
            org.springframework.data.domain.Pageable pageable,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        var profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        var page = service.getWardComplaints(profile.getWard().getWardId(), pageable);
        
        return ResponseEntity.ok(java.util.Map.of(
            "content", page.getContent().stream()
                .map(c -> new com.example.CivicConnect.dto.AdminComplaintDTO(
                    c.getComplaintId(),
                    c.getTitle(),
                    c.getStatus().name(),
                    c.getWard().getAreaName(),
                    c.getDepartment().getName(),
                    c.getPriority() != null ? c.getPriority().name() : "MEDIUM",
                    (c.getSla() != null && c.getSla().getStatus() != null) ? c.getSla().getStatus().name() : "ON_TRACK",
                    c.getCreatedAt().toString()
                )).toList(),
            "totalPages", page.getTotalPages(),
            "totalElements", page.getTotalElements()
        ));
    }


}
