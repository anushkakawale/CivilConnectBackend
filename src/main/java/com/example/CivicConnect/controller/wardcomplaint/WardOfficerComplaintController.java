package com.example.CivicConnect.controller.wardcomplaint;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintDecisionDTO;
import com.example.CivicConnect.dto.ApprovalQueueDTO;
import com.example.CivicConnect.dto.AdminComplaintDTO;
import com.example.CivicConnect.dto.ComplaintClosureDTO;
import com.example.CivicConnect.dto.ClosedComplaintTrackingDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.service.SharedComplaintService;
import com.example.CivicConnect.service.wardcomplaint.WardOfficerComplaintService;

@RestController
@RequestMapping("/api/ward-officer/complaints")
public class WardOfficerComplaintController {

    private final WardOfficerComplaintService service;
    private final OfficerProfileRepository officerProfileRepository;
    private final SharedComplaintService sharedService;

    public WardOfficerComplaintController(
            WardOfficerComplaintService service,
            OfficerProfileRepository officerProfileRepository,
            SharedComplaintService sharedService) {
        this.service = service;
        this.officerProfileRepository = officerProfileRepository;
        this.sharedService = sharedService;
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<?> getDetails(@PathVariable Long complaintId) {
        return ResponseEntity.ok(sharedService.getComplaintDetails(complaintId));
    }

    @PutMapping("/{complaintId}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long complaintId,
            @RequestBody ComplaintDecisionDTO dto,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        service.approve(complaintId, user, dto.getRemarks());
        return ResponseEntity.ok(Map.of("message", "Complaint APPROVED successfully"));
    }

    @PutMapping("/{complaintId}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long complaintId,
            @RequestBody ComplaintDecisionDTO dto,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        service.reject(complaintId, user, dto.getRemarks());
        return ResponseEntity.ok(Map.of("message", "Work REJECTED. Complaint sent back to Department Officer."));
    }

    @PutMapping("/{complaintId}/assign")
    public ResponseEntity<?> assign(
            @PathVariable Long complaintId,
            @RequestBody Map<String, Long> payload,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        Long officerId = payload.get("officerId");
        service.assign(complaintId, officerId, user);
        return ResponseEntity.ok(Map.of("message", "Complaint ASSIGNED successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        var profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        return ResponseEntity.ok(service.getWardStats(profile.getWard().getWardId()));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getWardComplaints(
            Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        var profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        Page<Complaint> page = service.getWardComplaints(profile.getWard().getWardId(), pageable);
        
        List<AdminComplaintDTO> content = page.getContent().stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unassigned")
    public ResponseEntity<?> getUnassigned(
            Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        var profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        Page<Complaint> page = service.getUnassignedComplaints(profile.getWard().getWardId(), pageable);
        
        List<AdminComplaintDTO> content = page.getContent().stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<?> pendingApproval(
            Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        var profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        Page<Complaint> page = service.getComplaintsForApproval(profile.getWard().getWardId(), pageable);

        List<ApprovalQueueDTO> content = page.getContent().stream()
                .map(c -> ApprovalQueueDTO.builder()
                        .id(c.getComplaintId())
                        .title(c.getTitle())
                        .departmentName(c.getDepartment().getName())
                        .priority(c.getPriority() != null ? c.getPriority().name() : "MEDIUM")
                        .resolvedBy(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Unknown")
                        .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : "ON_TRACK")
                        .resolvedAt(c.getUpdatedAt())
                        .resolvedRemarks("Verification Pending")
                        .build())
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/closed-history")
    public ResponseEntity<?> closedHistory(
            Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        var profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        Page<ComplaintClosureDTO> page = service.getClosedHistoryForWard(profile.getWard().getWardId(), pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    // 🆕 NEW: Detailed tracking for closed complaints (like approval queue)
    @GetMapping("/closed-tracking")
    public ResponseEntity<?> closedTracking(
            Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        var profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        Page<ClosedComplaintTrackingDTO> page = service.getClosedComplaintsTracking(profile.getWard().getWardId(), pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        response.put("currentPage", page.getNumber());
        
        return ResponseEntity.ok(response);
    }

    private AdminComplaintDTO toAdminDto(Complaint c) {
        String priorityStr = (c.getPriority() != null) ? c.getPriority().name() : "MEDIUM";
        String slaStatusStr = (c.getSla() != null && c.getSla().getStatus() != null) 
            ? c.getSla().getStatus().name() : "ON_TRACK";
        String slaDeadlineStr = (c.getSla() != null && c.getSla().getSlaDeadline() != null) 
            ? c.getSla().getSlaDeadline().toString() : null;
            
        return new AdminComplaintDTO(
            c.getComplaintId(),
            c.getTitle(),
            c.getStatus() != null ? c.getStatus().name() : "SUBMITTED",
            c.getWard() != null ? c.getWard().getAreaName() : "Unknown",
            c.getDepartment() != null ? c.getDepartment().getName() : "Unknown",
            priorityStr,
            slaStatusStr,
            slaDeadlineStr,
            c.getCreatedAt() != null ? c.getCreatedAt().toString() : null
        );
    }
}
