package com.example.CivicConnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizens")
@RequiredArgsConstructor
public class CitizenController {

	private final CitizenProfileRepository profileRepo;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {

        CitizenProfile profile = profileRepo
            .findByUser_UserId(user.getUserId())
            .orElseThrow(() -> new RuntimeException("Profile not found"));

        return ResponseEntity.ok(Map.of(
            "userId", user.getUserId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "mobile", user.getMobile(),
            "ward", profile.getWard() != null
                    ? profile.getWard().getWardNumber()
                    : null
        ));
    }
}
