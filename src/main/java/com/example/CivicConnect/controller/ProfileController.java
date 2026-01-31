package com.example.CivicConnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.PasswordUpdateDTO;
import com.example.CivicConnect.dto.ProfileResponseDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.UserProfileService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    // 👤 VIEW PROFILE
    @GetMapping
    public ProfileResponseDTO viewProfile(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return userProfileService.getProfile(user);
    }

    // ✏ UPDATE NAME
    @PutMapping("/name")
    public ResponseEntity<String> updateName(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        userProfileService.updateName(user, body.get("name"));
        return ResponseEntity.ok("Name updated successfully");
    }

    // 🔐 CHANGE PASSWORD
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @RequestBody PasswordUpdateDTO dto,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        userProfileService.updatePassword(user, dto);
        return ResponseEntity.ok("Password updated successfully");
    }
}
