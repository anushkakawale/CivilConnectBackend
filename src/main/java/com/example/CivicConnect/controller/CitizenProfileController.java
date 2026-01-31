package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.CitizenAddressDTO;
import com.example.CivicConnect.dto.CitizenProfileUpdateDTO;
import com.example.CivicConnect.dto.ProfileResponseDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.CitizenProfileService;
import com.example.CivicConnect.service.WardChangeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile/citizen")
@PreAuthorize("hasRole('CITIZEN')")
@RequiredArgsConstructor
public class CitizenProfileController {

    private final CitizenProfileService citizenProfileService;
    private final WardChangeService wardChangeService;

    // 👤 VIEW CITIZEN PROFILE
    @GetMapping
    public ProfileResponseDTO getProfile(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return citizenProfileService.getProfile(user);
    }

    // 🏘 REQUEST WARD CHANGE
    @PutMapping("/ward")
    public ResponseEntity<String> updateWard(
            @RequestBody CitizenProfileUpdateDTO dto,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        wardChangeService.createWardChangeRequest(user, dto.getWardId());

        return ResponseEntity.ok("Ward change request submitted");
    }
    @PutMapping("/address")
    public ResponseEntity<?> updateAddress(
            @RequestBody CitizenAddressDTO dto,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        citizenProfileService.updateAddress(user, dto);

        return ResponseEntity.ok("Address updated successfully");
    }
}

/*
@RestController
@RequestMapping("/api/profile")
public class CitizenProfileController {

    private final UserProfileService userProfileService;
    private final CitizenProfileService citizenProfileService;
    private final OfficerProfileService officerProfileService;
    private final com.example.CivicConnect.service.OtpService otpService;

    public CitizenProfileController(
            UserProfileService userProfileService,
            CitizenProfileService citizenProfileService,
            OfficerProfileService officerProfileService,
            com.example.CivicConnect.service.OtpService otpService) {

        this.userProfileService = userProfileService;
        this.citizenProfileService = citizenProfileService;
        this.officerProfileService = officerProfileService;
        this.otpService = otpService;
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

//    // MOBILE NUMBER UPDATE FLOW

   // 1. Send OTP to CURRENT User (Identity Check)
   @PostMapping("/identity-otp/send")
   public ResponseEntity<?> sendIdentityOtp(Authentication auth) {
       User user = (User) auth.getPrincipal();
        String message = otpService.generateOtpForCurrentUser(user);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // 2. Verify OTP for CURRENT User
    @PostMapping("/identity-otp/verify")
    public ResponseEntity<?> verifyIdentityOtp(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        String otp = body.get("otp");
        
        boolean verified = otpService.verifyCurrentUserOtp(user, otp);
        if (verified) {
            return ResponseEntity.ok(Map.of("message", "Identity verified", "verified", true));
       }
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP"));
   }
//
//    // 3. Send OTP to NEW Mobile
    @PostMapping("/mobile-otp/send")
    public ResponseEntity<?> sendNewMobileOtp(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        String newMobile = body.get("newMobile");
        
        try {
            String message = otpService.generateOtp(user, newMobile);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
//
    // 4. Verify OTP and Update Mobile
    @PostMapping("/mobile-otp/verify")
    public ResponseEntity<?> verifyNewMobileOtp(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        String newMobile = body.get("newMobile");
        String otp = body.get("otp");
        
        try {
            boolean success = otpService.verifyOtp(user, newMobile, otp);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Mobile number updated successfully"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.badRequest().body("Validation failed");
    }
}*/
