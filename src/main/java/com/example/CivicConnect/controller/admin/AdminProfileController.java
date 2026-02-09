package com.example.CivicConnect.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.UserProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/profile")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProfileController {

    private final UserProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getAdminProfile(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(profileService.getAdminProfile(user));
    }
}
