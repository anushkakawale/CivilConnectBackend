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
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    // 👤 VIEW PROFILE (All Roles: Returns Admin/Officer/Citizen details + Completion Score)
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

    // 📊 COMPLETION SCORE
    @GetMapping("/completion-score")
    public ResponseEntity<Integer> getCompletionScore(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(userProfileService.calculateCompletionScore(user));
    }
    // 📸 UPLOAD PROFILE IMAGE
    @org.springframework.web.bind.annotation.PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication auth) {
            
        User user = (User) auth.getPrincipal();
        String imageUrl = userProfileService.updateProfileImage(user, file);
        
        return ResponseEntity.ok(Map.of(
            "message", "Profile image updated successfully",
            "imageUrl", imageUrl
        ));
    }
}
