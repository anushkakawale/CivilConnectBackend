package com.example.CivicConnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.MobileOtpService;

@RestController
@RequestMapping("/api/profile/mobile")
public class MobileOtpController {

    private final MobileOtpService otpService;

    public MobileOtpController(MobileOtpService otpService) {
        this.otpService = otpService;
    }

    // REQUEST OTP
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(
            @RequestParam String newMobile,
            Authentication auth) {

        User user = (User) auth.getPrincipal();

        String otp = otpService.requestOtp(user, newMobile);

        return ResponseEntity.ok(
                Map.of(
                        "message", "OTP sent successfully",
                        "mockOtp", otp // ⚠️ REMOVE in production
                )
        );
    }

    // VERIFY OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String otp,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        otpService.verifyOtp(user, otp);

        return ResponseEntity.ok("Mobile number updated successfully");
    }
}
