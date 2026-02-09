package com.example.CivicConnect.controller.departmentcomplaint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.departmentcomplaint.DepartmentComplaintService;

@RestController
@RequestMapping("/api/department/complaints")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('DEPARTMENT_OFFICER')")
public class DepartmentComplaintController {

    private final DepartmentComplaintService service;
    private final com.example.CivicConnect.service.SharedComplaintService sharedService;
    private final com.example.CivicConnect.service.DepartmentDashboardService dashboardService;

    public DepartmentComplaintController(
            DepartmentComplaintService service, 
            com.example.CivicConnect.service.SharedComplaintService sharedService,
            com.example.CivicConnect.service.DepartmentDashboardService dashboardService) {
        this.service = service;
        this.sharedService = sharedService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<?> getComplaints(
            org.springframework.data.domain.Pageable pageable,
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.util.List<com.example.CivicConnect.entity.enums.ComplaintStatus> status,
            Authentication auth) {
        
        User officer = (User) auth.getPrincipal();
        
        // If no status provided, return all relevant statuses
        if (status == null || status.isEmpty()) {
            status = java.util.List.of(
                com.example.CivicConnect.entity.enums.ComplaintStatus.ASSIGNED, 
                com.example.CivicConnect.entity.enums.ComplaintStatus.IN_PROGRESS, 
                com.example.CivicConnect.entity.enums.ComplaintStatus.RESOLVED, 
                com.example.CivicConnect.entity.enums.ComplaintStatus.ON_HOLD, 
                com.example.CivicConnect.entity.enums.ComplaintStatus.ESCALATED,
                com.example.CivicConnect.entity.enums.ComplaintStatus.REOPENED,
                com.example.CivicConnect.entity.enums.ComplaintStatus.APPROVED,
                com.example.CivicConnect.entity.enums.ComplaintStatus.CLOSED,
                com.example.CivicConnect.entity.enums.ComplaintStatus.REJECTED
            );
        }
        
        return ResponseEntity.ok(dashboardService.getAssignedComplaints(officer.getUserId(), status, pageable));
    }

    @GetMapping("/assigned")
    public ResponseEntity<?> getAssigned(
            org.springframework.data.domain.Pageable pageable,
            Authentication auth) {
        User officer = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getAssignedComplaints(officer.getUserId(), pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(Authentication auth) {
        User officer = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getOfficerSummary(officer.getUserId()));
    }

    @GetMapping("/peers")
    public ResponseEntity<?> getPeers(Authentication auth) {
        User officer = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getPeerComplaints(officer.getUserId()));
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<?> getDetails(@PathVariable Long complaintId) {
        return ResponseEntity.ok(sharedService.getComplaintDetails(complaintId));
    }

    // ▶ START WORK (DEPARTMENT OFFICER)
    @PutMapping("/{complaintId}/start")
    public ResponseEntity<?> start(
            @PathVariable Long complaintId,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal();
        service.startWork(complaintId, officer);

        return ResponseEntity.ok("Complaint marked IN_PROGRESS");
    }

    // ▶ RESOLVE WORK (DEPARTMENT OFFICER)
    @PutMapping("/{complaintId}/resolve")
    public ResponseEntity<?> resolve(
            @PathVariable Long complaintId,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal();
        service.resolve(complaintId, officer);

        return ResponseEntity.ok("Complaint marked RESOLVED");
    }

}
