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
    // GENERATE OTP
    // ===============================
    public String generateOtp(User user, String newMobile) {
        // Validate mobile number
        if (newMobile == null || newMobile.length() != 10) {
            throw new RuntimeException("Invalid mobile number");
        }

        // Check if mobile already exists
        if (userRepository.findByMobile(newMobile).isPresent()) {
            throw new RuntimeException("Mobile number already registered");
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Create OTP record
        MobileOtp mobileOtp = MobileOtp.builder()
                .user(user)
                .newMobile(newMobile)
                .otp(otp)
                .verified(false)
                .used(false)
                .build();

        otpRepository.save(mobileOtp);

        // Send notification (in real system, send SMS)
        notificationService.notifyUser(
                user,
                "Mobile OTP",
                "Your OTP for mobile number change is: " + otp + ". Valid for 10 minutes."
        );

        // In production, send actual SMS here
        System.out.println("OTP for " + newMobile + ": " + otp);

        return "OTP sent to " + newMobile;
    }

    // ===============================
    // VERIFY OTP
    // ===============================
    public boolean verifyOtp(User user, String newMobile, String otp) {
        // Find valid OTP
        MobileOtp mobileOtp = otpRepository.findValidOtp(user, newMobile, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

        // Verify OTP
        if (!mobileOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Incorrect OTP");
        }

        // Mark as verified
        mobileOtp.setVerified(true);
        mobileOtp.setUsed(true);
        otpRepository.save(mobileOtp);

        // Update user mobile
        user.setMobile(newMobile);
        userRepository.save(user);

        // Notify success
        notificationService.notifyUser(
                user,
                "Mobile Updated",
                "Your mobile number has been successfully updated to " + newMobile
        );

        return true;
    }

    // ===============================
    // CLEANUP EXPIRED OTPs (Scheduled)
    // ===============================
    public int cleanupExpiredOtps() {
        return otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}
