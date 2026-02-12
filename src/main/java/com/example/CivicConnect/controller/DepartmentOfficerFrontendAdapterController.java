package com.example.CivicConnect.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.DepartmentDashboardService;
import com.example.CivicConnect.service.MapComplaintService;

import lombok.RequiredArgsConstructor;

/**
 * Adapter Controller to clear frontend 403 errors by mapping legacy paths
 * to existing services.
 * 
 * Maps: /api/department-officer/** -> existing service logic
 */
@RestController
@RequestMapping("/api/department-officer")
@PreAuthorize("hasRole('DEPARTMENT_OFFICER')")
@RequiredArgsConstructor
public class DepartmentOfficerFrontendAdapterController {

    private final DepartmentDashboardService dashboardService;
    private final MapComplaintService mapService;

    // 1. Analytics Dashboard
    // Frontend calls: /api/department-officer/analytics/dashboard
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<?> getDashboardSummary(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getOfficerSummary(user.getUserId()));
    }

    // 2. Assigned Complaints
    // Frontend calls: /api/department-officer/complaints
    @GetMapping("/complaints")
    public ResponseEntity<?> getAssignedComplaints(Authentication auth, Pageable pageable) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getAssignedComplaints(user.getUserId(), pageable));
    }

    // 3. Map Data
    // Frontend calls: /api/department-officer/map
    @GetMapping("/map")
    public ResponseEntity<?> getMapData(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(mapService.getMapComplaints(user, null));
    }
}
