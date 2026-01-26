package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.WardOfficerAnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ward-officer/analytics")
@RequiredArgsConstructor
public class WardOfficerAnalyticsController {

    private final WardOfficerAnalyticsService analyticsService;

    @GetMapping("/department-wise")
    public ResponseEntity<?> getDepartmentWiseAnalytics(Authentication auth) {
        User wardOfficer = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                analyticsService.getDepartmentWiseAnalytics(wardOfficer.getUserId())
        );
    }

    @GetMapping("/sla")
    public ResponseEntity<?> getSlaAnalytics(Authentication auth) {
        User wardOfficer = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                analyticsService.getSlaAnalytics(wardOfficer.getUserId())
        );
    }

    @GetMapping("/officer-workload")
    public ResponseEntity<?> getOfficerWorkload(Authentication auth) {
        User wardOfficer = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                analyticsService.getOfficerWorkloadAnalytics(wardOfficer.getUserId())
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getWardSummary(Authentication auth) {
        User wardOfficer = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                analyticsService.getWardSummary(wardOfficer.getUserId())
        );
    }
}
