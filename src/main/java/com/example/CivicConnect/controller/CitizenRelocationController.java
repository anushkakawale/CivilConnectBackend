package com.example.CivicConnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.WardChangeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizens")
@PreAuthorize("hasRole('CITIZEN')")
@RequiredArgsConstructor
public class CitizenRelocationController {

    private final WardChangeService wardChangeService;

    /**
     * POST /api/citizens/ward-change-request
     * Handles both first-time ward assignment and subsequent relocation requests.
     */
    @PostMapping("/ward-change-request")
    public ResponseEntity<?> requestWardChange(
            @RequestBody com.example.CivicConnect.dto.WardChangeRequestPayload payload,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        Long newWardId = payload.getWardId();
        
        if (newWardId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "wardId is required"));
        }
        
        try {
            wardChangeService.createWardChangeRequest(user, newWardId, payload.getReason());
            return ResponseEntity.ok(Map.of("message", "Relocation request processed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
