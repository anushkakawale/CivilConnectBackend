package com.example.CivicConnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.DashboardCountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardCountService dashboardCountService;

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getCounts(Authentication auth) {

        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                dashboardCountService.getDashboardCounts(user)
        );
    }
}
