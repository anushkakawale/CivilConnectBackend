package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;

@RestController
@RequestMapping("/api/citizen")
public class CitizenController {

    @GetMapping("/profile")
    public ResponseEntity<String> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok("Welcome " + user.getName() + ", role: " + user.getRole());
    }
}
