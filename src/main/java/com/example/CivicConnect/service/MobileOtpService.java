package com.example.CivicConnect.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.MobileOtp;
import com.example.CivicConnect.repository.MobileOtpRepository;
import com.example.CivicConnect.repository.UserRepository;

@Service
@Transactional
public class MobileOtpService {

    private final MobileOtpRepository otpRepository;
    private final UserRepository userRepository;

    public MobileOtpService(
            MobileOtpRepository otpRepository,
            UserRepository userRepository) {

        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
    }

    // ==============================
    // 1️⃣ REQUEST OTP
    // ==============================
    public String requestOtp(User user, String newMobile) {

        // 🔐 Optional: Prevent same mobile reuse
        if (newMobile.equals(user.getMobile())) {
            throw new RuntimeException("New mobile number is same as current");
        }

        // 🔢 Generate 6-digit OTP
        String otp = String.valueOf(
                100000 + new Random().nextInt(900000)
        );

        MobileOtp entity = new MobileOtp();
        entity.setUser(user);
        entity.setNewMobile(newMobile);
        entity.setOtp(otp);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        entity.setVerified(false);

        otpRepository.save(entity);

        // 🚨 In production → send SMS here
        return otp; // mock OTP for testing
    }

    // ==============================
    // 2️⃣ VERIFY OTP
    // ==============================
    public void verifyOtp(User user, String otp) {

        MobileOtp record = otpRepository
                .findTopByUserAndVerifiedFalseOrderByOtpIdDesc(user)
                .orElseThrow(() ->
                        new RuntimeException("OTP not found"));

        // ⏱ Expiry check
        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        // OTP mismatch
        if (!record.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // Mark verified
        record.setVerified(true);

        // Update user's mobile
        user.setMobile(record.getNewMobile());
        userRepository.save(user);

        otpRepository.save(record);
    }
}
