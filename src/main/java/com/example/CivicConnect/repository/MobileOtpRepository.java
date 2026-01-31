package com.example.CivicConnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.MobileOtp;

public interface MobileOtpRepository extends JpaRepository<MobileOtp, Long> {

    // Find latest OTP for user
    Optional<MobileOtp> findTopByUserAndVerifiedFalseOrderByOtpIdDesc(User user);
    
    // Find latest OTP for user and mobile
    Optional<MobileOtp> findTopByUserAndNewMobileOrderByCreatedAtDesc(User user, String newMobile);

    // Find all OTPs for a user
    List<MobileOtp> findByUserOrderByCreatedAtDesc(User user);

    // Delete expired OTPs (cleanup)
    @Modifying
    @Query("DELETE FROM MobileOtp o WHERE o.expiresAt < :now")
    int deleteExpiredOtps(@Param("now") LocalDateTime now);

    // Find unused and non-expired OTP
    @Query("SELECT o FROM MobileOtp o WHERE o.user = :user AND o.newMobile = :mobile AND o.used = false AND o.verified = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<MobileOtp> findValidOtp(@Param("user") User user, @Param("mobile") String mobile, @Param("now") LocalDateTime now);

    // Find valid OTP for current user (where newMobile is NULL)
    @Query("SELECT o FROM MobileOtp o WHERE o.user = :user AND o.newMobile IS NULL AND o.used = false AND o.verified = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<MobileOtp> findValidOtpForCurrentUser(@Param("user") User user, @Param("now") LocalDateTime now);
}
