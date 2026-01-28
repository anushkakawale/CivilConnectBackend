package com.example.CivicConnect.controller.profile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.CitizenProfileResponseDTO;
import com.example.CivicConnect.dto.CitizenProfileUpdateDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.CitizenProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile/citizen")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CITIZEN')")
public class CitizenProfileController {

    private final CitizenProfileService citizenProfileService;

    @GetMapping
    public ResponseEntity<CitizenProfileResponseDTO> getProfile(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
            citizenProfileService.getCitizenProfile(user)
        );
    }

    @PutMapping
    public ResponseEntity<String> updateProfile(
            Authentication auth,
            @RequestBody CitizenProfileUpdateDTO dto) {

        User user = (User) auth.getPrincipal();
        citizenProfileService.updateProfile(user.getUserId(), dto);
        return ResponseEntity.ok("Profile updated successfully");
    }
}
