package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.OfficerDirectoryService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/department/officers")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('DEPARTMENT_OFFICER')")
@RequiredArgsConstructor
public class DepartmentOfficerController {

    private final OfficerDirectoryService service;
    private final com.example.CivicConnect.service.DepartmentDashboardService1 dashboardService;

    // 🏢 Department Officer → View his Ward Officer
    @GetMapping("/ward-officer")
    public ResponseEntity<?> wardOfficer(Authentication auth) {

        User officer = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                service.getWardOfficerForDepartmentOfficer(officer)
        );
    }
    
    // ✅ NEW: View other officers in same department & ward
    @GetMapping("/colleagues")
    public ResponseEntity<?> colleagues(Authentication auth) {
        User officer = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getDepartmentColleagues(officer.getUserId()));
    }
}
