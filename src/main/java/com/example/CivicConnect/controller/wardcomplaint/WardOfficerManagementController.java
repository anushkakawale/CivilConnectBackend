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
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/ward-officer/management")
public class WardOfficerManagementController {

    private final DepartmentOfficerRegistrationService deptRegistrationService;
    private final WardChangeService wardChangeService;
    private final OfficerProfileRepository officerProfileRepository;
    private final WardOfficerAnalyticsService analyticsService;
    private final ComplaintRepository complaintRepository;

    public WardOfficerManagementController(
            DepartmentOfficerRegistrationService deptRegistrationService,
            WardChangeService wardChangeService,
            OfficerProfileRepository officerProfileRepository,
            WardOfficerAnalyticsService analyticsService,
            ComplaintRepository complaintRepository) {
        this.deptRegistrationService = deptRegistrationService;
        this.wardChangeService = wardChangeService;
        this.officerProfileRepository = officerProfileRepository;
        this.analyticsService = analyticsService;
        this.complaintRepository = complaintRepository;
    }

    @PostMapping("/register-officer")
    public ResponseEntity<?> registerOfficer(
            @Valid @RequestBody DepartmentOfficerRegistrationDTO dto,
            Authentication auth) {

        User wardOfficer = (User) auth.getPrincipal();
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
                .orElseThrow(() -> new RuntimeException("Logged in officer profile not found"));

        if (profile.getWard() == null) {
            throw new RuntimeException("You are not assigned to any ward, cannot register officers.");
        }

        dto.setWardId(profile.getWard().getWardId());
        return ResponseEntity.ok(deptRegistrationService.registerDepartmentOfficer(dto));
    }

    @GetMapping("/ward-changes")
    public ResponseEntity<?> getWardChanges(Authentication auth) {
        User wardOfficer = (User) auth.getPrincipal();
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        List<WardChangeRequest> requests = wardChangeService.getPendingForWard(profile.getWard());
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/ward-changes/{requestId}/approve")
    public ResponseEntity<?> approveWardChange(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        
        User officer = (User) auth.getPrincipal();
        String remarks = body.getOrDefault("remarks", "Approved by Ward Officer");
        wardChangeService.approveWardChange(requestId, officer, remarks);
        return ResponseEntity.ok(Map.of("message", "Request approved successfully"));
    }

    @PutMapping("/ward-changes/{requestId}/reject")
    public ResponseEntity<?> rejectWardChange(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        
        User officer = (User) auth.getPrincipal();
        String remarks = body.getOrDefault("remarks", "Rejected by Ward Officer");
        wardChangeService.rejectWardChange(requestId, officer, remarks);
        return ResponseEntity.ok(Map.of("message", "Request rejected successfully"));
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> summary = analyticsService.getWardSummary(user.getUserId());
        Map<String, Object> deptStats = analyticsService.getDepartmentWiseAnalytics(user.getUserId());
        Map<String, Object> slaStats = analyticsService.getSlaAnalytics(user.getUserId());
        Map<String, Object> workload = analyticsService.getOfficerWorkloadAnalytics(user.getUserId());
        
        result.putAll(summary);
        result.putAll(deptStats);
        result.putAll(slaStats);
        result.putAll(workload);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/officers")
    public ResponseEntity<?> getOfficers(Authentication auth) {
        User wardOfficer = (User) auth.getPrincipal();
        return ResponseEntity.ok(
            officerProfileRepository.findByWard_WardIdAndUser_Role(
                officerProfileRepository.findByUser_UserId(wardOfficer.getUserId())
                    .orElseThrow(() -> new RuntimeException("Profile not found")).getWard().getWardId(),
                com.example.CivicConnect.entity.enums.RoleName.DEPARTMENT_OFFICER
            ).stream()
            .map(p -> new com.example.CivicConnect.dto.AdminOfficerDTO(
                p.getUser().getUserId(),
                p.getUser().getName(),
                p.getUser().getRole().name(),
                p.getWard() != null ? p.getWard().getAreaName() : "-",
                p.getDepartment() != null ? p.getDepartment().getName() : "-",
                p.getUser().getEmail(),
                p.getUser().getMobile(),
                p.getUser().isActive(),
                p.getUser().getCreatedAt() != null ? p.getUser().getCreatedAt().toString() : "N/A"
            ))
            .collect(Collectors.toList())
        );
    }

    @GetMapping("/complaints")
    public ResponseEntity<?> getComplaints(Pageable pageable, Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));
        
        Page<Complaint> page = complaintRepository.findByWard_WardId(profile.getWard().getWardId(), pageable);
        
        List<AdminComplaintDTO> dtos = page.getContent().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
            
        Map<String, Object> response = new HashMap<>();
        response.put("content", dtos);
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        
        return ResponseEntity.ok(response);
    }
    
    private AdminComplaintDTO toDto(Complaint c) {
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
