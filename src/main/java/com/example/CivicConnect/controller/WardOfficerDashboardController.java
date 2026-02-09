package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.WardOfficerDashboardService;

@RestController
@RequestMapping("/api/ward-officer/dashboard")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('WARD_OFFICER')")
public class WardOfficerDashboardController {

    private final WardOfficerDashboardService service;

    public WardOfficerDashboardController(
            WardOfficerDashboardService service) {
        this.service = service;
    }

    @GetMapping("/pending-approvals")
    public List<ComplaintApproval> pendingApprovals(org.springframework.security.core.Authentication auth) {
        User user = (User) auth.getPrincipal();
        return service.getPendingApprovals(user.getUserId());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(org.springframework.security.core.Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.getWardStats(user.getUserId()));
    }

    @GetMapping("/complaints")
    public ResponseEntity<?> getWardComplaints(org.springframework.security.core.Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(service.getAllWardComplaints(user.getUserId()));
    }
}
