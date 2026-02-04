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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/department/dashboard")
@PreAuthorize("hasRole('DEPARTMENT_OFFICER')")
@RequiredArgsConstructor
public class DepartmentDashboardController {

    private final DepartmentDashboardService dashboardService;

    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedWork(Authentication auth, Pageable pageable) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getAssignedComplaints(user.getUserId(), pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(dashboardService.getOfficerSummary(user.getUserId()));
    }
}