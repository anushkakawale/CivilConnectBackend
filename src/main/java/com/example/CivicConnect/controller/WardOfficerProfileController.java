package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.UserProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ward-officer/profile")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('WARD_OFFICER')")
@RequiredArgsConstructor
public class WardOfficerProfileController {

    private final UserProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getOfficerProfile(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(profileService.getOfficerProfile(user));
    }
}
