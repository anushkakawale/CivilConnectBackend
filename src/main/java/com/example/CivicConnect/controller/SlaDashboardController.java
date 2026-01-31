package com.example.CivicConnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.SlaDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard/sla")
@RequiredArgsConstructor
public class SlaDashboardController {

    private final SlaDashboardService slaDashboardService;

    @GetMapping
    public ResponseEntity<Map<String, Long>> getSlaStats(Authentication auth) {

        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                slaDashboardService.getSlaStats(user)
        );
    }
}
