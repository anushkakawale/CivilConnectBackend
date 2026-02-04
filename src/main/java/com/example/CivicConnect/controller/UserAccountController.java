package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.UserAccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/deactivate")
    public ResponseEntity<?> deactivate(Authentication auth) {
        User user = (User) auth.getPrincipal();
        userAccountService.deactivate(user);
        return ResponseEntity.ok("Account deactivated");
    }

    // ✅ NEW: Admin Deactivate User
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivate/{userId}")
    public ResponseEntity<?> deactivateUser(
            @org.springframework.web.bind.annotation.PathVariable Long userId) {
        
        userAccountService.deactivateUserById(userId);
        return ResponseEntity.ok("User account deactivated");
    }
}
