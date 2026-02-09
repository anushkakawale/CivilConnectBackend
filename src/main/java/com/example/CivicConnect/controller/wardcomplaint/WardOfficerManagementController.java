package com.example.CivicConnect.controller.wardcomplaint;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.AdminComplaintDTO;
import com.example.CivicConnect.dto.DepartmentOfficerRegistrationDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.entity.profiles.WardChangeRequest;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.service.DepartmentOfficerRegistrationService;
import com.example.CivicConnect.service.WardChangeService;
import com.example.CivicConnect.service.WardOfficerAnalyticsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ward-officer/management")
@PreAuthorize("hasRole('WARD_OFFICER')")
@RequiredArgsConstructor
public class WardOfficerManagementController {

    private final DepartmentOfficerRegistrationService deptRegistrationService;
    private final WardChangeService wardChangeService;
    private final OfficerProfileRepository officerProfileRepository;
    private final WardOfficerAnalyticsService analyticsService;
    private final ComplaintRepository complaintRepository;

    // 1️⃣ REGISTER DEPARTMENT OFFICER (FOR THIS WARD ONLY)
    @PostMapping("/register-officer")
    public ResponseEntity<?> registerOfficer(
            @RequestBody DepartmentOfficerRegistrationDTO dto,
            Authentication auth) {

        User wardOfficer = (User) auth.getPrincipal();
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        // Enforce ward restriction
        dto.setWardId(profile.getWard().getWardId());
        
        return ResponseEntity.ok(deptRegistrationService.registerDepartmentOfficer(dto));
    }

    // 2️⃣ VIEW WARD CHANGE REQUESTS
    @GetMapping("/ward-changes")
    public ResponseEntity<?> getWardChanges(Authentication auth) {
        User wardOfficer = (User) auth.getPrincipal();
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        List<WardChangeRequest> requests = wardChangeService.getPendingForWard(profile.getWard());
        return ResponseEntity.ok(requests);
    }

    // 3️⃣ APPROVE/REJECT WARD CHANGE
    @PutMapping("/ward-changes/{requestId}/approve")
    public ResponseEntity<?> approveWardChange(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        
        User officer = (User) auth.getPrincipal();
        wardChangeService.approveWardChange(requestId, officer, body.getOrDefault("remarks", "Approved by Ward Officer"));
        return ResponseEntity.ok(Map.of("message", "Request approved successfully"));
    }

    @PutMapping("/ward-changes/{requestId}/reject")
    public ResponseEntity<?> rejectWardChange(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        
        User officer = (User) auth.getPrincipal();
        wardChangeService.rejectWardChange(requestId, officer, body.getOrDefault("remarks", "Rejected by Ward Officer"));
        return ResponseEntity.ok(Map.of("message", "Request rejected successfully"));
    }

    // 4️⃣ WARD ANALYTICS
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        var summary = analyticsService.getWardSummary(user.getUserId());
        var deptStats = analyticsService.getDepartmentWiseAnalytics(user.getUserId());
        var slaStats = analyticsService.getSlaAnalytics(user.getUserId());
        var workload = analyticsService.getOfficerWorkloadAnalytics(user.getUserId());
        
        response.putAll(summary);
        response.putAll(deptStats);
        response.putAll(slaStats);
        response.putAll(workload);
        
        return ResponseEntity.ok(response);
    }

    // 5️⃣ VIEW ALL COMPLAINTS IN WARD (Management List)
    @GetMapping("/complaints")
    public ResponseEntity<?> getComplaints(Pageable pageable, Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        Page<Complaint> page = complaintRepository.findByWard_WardId(profile.getWard().getWardId(), pageable);
        
        return ResponseEntity.ok(Map.of(
            "content", page.getContent().stream()
                .map(c -> new AdminComplaintDTO(
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
