package com.example.CivicConnect.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.MobileOtp;
import com.example.CivicConnect.repository.MobileOtpRepository;
import com.example.CivicConnect.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {

    private final MobileOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ===============================
    // STEP 1️⃣ SEND OTP TO REGISTERED MOBILE
    // ===============================
    public void sendOtpToOldMobile(User user, String newMobile) {

        // Check if new mobile is already taken by someone else
        if (newMobile != null && !newMobile.isBlank()) {
            if (userRepository.findByMobile(newMobile).isPresent()) {
                throw new RuntimeException("Mobile number already registered by another user");
            }
        }

        // Generate 6 digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Save entry
        MobileOtp mobileOtp = MobileOtp.builder()
                .user(user)
                .newMobile(newMobile) // may be null initially
                .otp(otp)
                .verified(false)
                .used(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        otpRepository.save(mobileOtp);

        // Notify via simulated SMS (Notification Service)
        notificationService.notifyUser(
                user,
                "Profile Security OTP",
                "Your identity verification OTP is: " + otp + ". This is valid for 10 minutes."
        );

        // ✅ MANDATORY LOG TO CONSOLE (As requested by USER)
        System.out.println("================================");
        System.out.println("DEBUG OTP FOR " + user.getName() + " (OLD: " + user.getMobile() + ")");
        System.out.println("OTP CODE: " + otp);
        System.out.println("================================");
    }

    // ===============================
    // STEP 2️⃣ VERIFY OTP & UPDATE MOBILE
    // ===============================
    public void verifyOtpAndUpdateMobile(User user, String otp) {

        MobileOtp mobileOtp = otpRepository
                .findTopByUserAndVerifiedFalseOrderByOtpIdDesc(user)
                .orElseThrow(() -> new RuntimeException("No pending OTP found for this user"));

        if (!mobileOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Incorrect OTP. Please check your messages.");
        }
        
        if (mobileOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        mobileOtp.setVerified(true);
        mobileOtp.setUsed(true);
        otpRepository.save(mobileOtp);

        // If new mobile was provided in Step 1, update it now
        if (mobileOtp.getNewMobile() != null && !mobileOtp.getNewMobile().isBlank()) {
            user.setMobile(mobileOtp.getNewMobile());
            userRepository.save(user);

            notificationService.notifyUser(
                    user,
                    "Mobile Updated",
                    "Your mobile number has been successfully updated to " + mobileOtp.getNewMobile()
            );
        }
    }
}

