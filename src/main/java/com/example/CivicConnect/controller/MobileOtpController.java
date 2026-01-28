package com.example.CivicConnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.MobileOtpRequestDTO;
import com.example.CivicConnect.dto.MobileOtpVerifyDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.OtpService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile/mobile")
@RequiredArgsConstructor
public class MobileOtpController {

    private final OtpService otpService;

    // REQUEST OTP
    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, String>> requestOtp(
            @Valid @RequestBody MobileOtpRequestDTO dto,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        String message = otpService.generateOtp(user, dto.getNewMobile());
        
        return ResponseEntity.ok(Map.of("message", message));
    }

    // VERIFY OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @Valid @RequestBody MobileOtpVerifyDTO dto,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        otpService.verifyOtp(user, dto.getNewMobile(), dto.getOtp());
        
        return ResponseEntity.ok(Map.of("message", "Mobile number updated successfully"));
    }
}
