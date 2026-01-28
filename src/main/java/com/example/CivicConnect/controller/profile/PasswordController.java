package com.example.CivicConnect.controller.profile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.PasswordUpdateDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.UserProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile/password")
@RequiredArgsConstructor
public class PasswordController {

    private final UserProfileService userProfileService;

    @PutMapping
    public ResponseEntity<String> updatePassword(
            Authentication auth,
            @RequestBody PasswordUpdateDTO dto) {

        User user = (User) auth.getPrincipal();
        userProfileService.updatePassword(user, dto);
        return ResponseEntity.ok("Password updated successfully");
    }
}
