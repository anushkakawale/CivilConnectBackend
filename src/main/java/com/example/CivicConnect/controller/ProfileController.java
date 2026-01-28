package com.example.CivicConnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.CitizenProfileUpdateDTO;
import com.example.CivicConnect.dto.OfficerProfileUpdateDTO;
import com.example.CivicConnect.dto.PasswordUpdateDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.CitizenProfileService;
import com.example.CivicConnect.service.OfficerProfileService;
import com.example.CivicConnect.service.UserProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserProfileService userProfileService;
    private final CitizenProfileService citizenProfileService;
    private final OfficerProfileService officerProfileService;

    public ProfileController(
            UserProfileService userProfileService,
            CitizenProfileService citizenProfileService,
            OfficerProfileService officerProfileService) {

        this.userProfileService = userProfileService;
        this.citizenProfileService = citizenProfileService;
        this.officerProfileService = officerProfileService;
    }

// Methods removed to resolve ambiguity and separate concerns.
    // Password update moved to PasswordController.
    // Citizen profile management moved to CitizenProfileController.

    // 🧑‍✈️ OFFICER PROFILE
    @PutMapping("/officer")
    public ResponseEntity<?> updateOfficerProfile(
            @RequestBody OfficerProfileUpdateDTO dto,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        officerProfileService.updateProfile(user.getUserId(), dto);

        return ResponseEntity.ok("Officer profile updated");
    }
}
