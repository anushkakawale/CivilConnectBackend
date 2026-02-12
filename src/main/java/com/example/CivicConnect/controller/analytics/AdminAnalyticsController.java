package com.example.CivicConnect.controller.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.service.AdminAnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboard());
    }

    @GetMapping("/officer-workload")
    public ResponseEntity<?> getOfficerWorkload() {
        return ResponseEntity.ok(analyticsService.getOfficerWorkload());
    }

    @GetMapping("/trends")
    public ResponseEntity<?> getTrends() {
        return ResponseEntity.ok(analyticsService.getDailyTrend());
    }

    @GetMapping("/ward-performance")
    public ResponseEntity<?> getWardPerformance() {
        return ResponseEntity.ok(analyticsService.getWardPerformance());
    }

    @GetMapping("/department-performance")
    public ResponseEntity<?> getDepartmentPerformance() {
        return ResponseEntity.ok(analyticsService.getDepartmentPerformance());
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(analyticsService.getCategoryDistribution());
    }

}
