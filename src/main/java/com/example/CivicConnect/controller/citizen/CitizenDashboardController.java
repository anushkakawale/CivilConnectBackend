package com.example.CivicConnect.controller.citizen;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.citizen.CitizenDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
public class CitizenDashboardController {

    private final CitizenDashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        Map<String, Object> dashboardData = dashboardService.getDashboardData(citizen);
        return ResponseEntity.ok(dashboardData);
    }
}
