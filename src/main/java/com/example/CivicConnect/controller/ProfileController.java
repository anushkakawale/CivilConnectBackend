package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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

    // 🔐 UPDATE PASSWORD (ALL ROLES)
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordUpdateDTO dto,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        userProfileService.updatePassword(user, dto);

        return ResponseEntity.ok("Password updated successfully");
    }

    // 👤 CITIZEN PROFILE
    @PutMapping("/citizen")
    public ResponseEntity<?> updateCitizenProfile(
            @RequestBody CitizenProfileUpdateDTO dto,
            Authentication auth) {
    	String email = auth.getName();
        User user = (User) auth.getPrincipal();
        citizenProfileService.updateProfile(user.getUserId(), dto);

        return ResponseEntity.ok("Citizen profile updated");
    }
    @GetMapping("/citizen")
    public ResponseEntity<?> viewCitizenProfile(Authentication auth) {

        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                citizenProfileService.getCitizenProfile(user)
        );
    }

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
