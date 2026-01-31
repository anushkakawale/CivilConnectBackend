package com.example.CivicConnect.service;

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
    // STEP 1️⃣ SEND OTP TO OLD MOBILE
    // ===============================
    public void sendOtpToOldMobile(User user, String newMobile) {

        if (newMobile == null || newMobile.length() != 10) {
            throw new RuntimeException("Invalid mobile number");
        }

        if (userRepository.findByMobile(newMobile).isPresent()) {
            throw new RuntimeException("Mobile number already registered");
        }

        // OTP sent to OLD mobile → newMobile stored for later
        String otp = createAndSaveOtp(user, newMobile);

        notificationService.notifyUser(
                user,
                "Mobile Change OTP",
                "Your OTP is: " + otp + ". Valid for 10 minutes."
        );

        System.out.println("OTP sent to OLD mobile: " + otp);
    }

    // ===============================
    // PRIVATE HELPER
    // ===============================
    private String createAndSaveOtp(User user, String newMobile) {

        String otp = String.format("%06d", new Random().nextInt(999999));

        MobileOtp mobileOtp = MobileOtp.builder()
                .user(user)
                .newMobile(newMobile) // store new mobile safely
                .otp(otp)
                .verified(false)
                .used(false)
                .build();

        otpRepository.save(mobileOtp);
        return otp;
    }

    // ===============================
    // STEP 2️⃣ VERIFY OTP & UPDATE MOBILE
    // ===============================
    public void verifyOtpAndUpdateMobile(User user, String otp) {

        MobileOtp mobileOtp = otpRepository
                .findTopByUserAndVerifiedFalseOrderByOtpIdDesc(user)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

        if (!mobileOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Incorrect OTP");
        }

        mobileOtp.setVerified(true);
        mobileOtp.setUsed(true);
        otpRepository.save(mobileOtp);

        // ✅ NOW update mobile
        user.setMobile(mobileOtp.getNewMobile());
        userRepository.save(user);

        notificationService.notifyUser(
                user,
                "Mobile Updated",
                "Your mobile number has been updated successfully"
        );
    }
}

