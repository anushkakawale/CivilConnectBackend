package com.example.CivicConnect.entity.system;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.core.User;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mobile_otp")
@Data
public class MobileOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long otpId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String newMobile;

    private String otp; // mock OTP

    private LocalDateTime expiresAt;

    private boolean verified = false;
}
