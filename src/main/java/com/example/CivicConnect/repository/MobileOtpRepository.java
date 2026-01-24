package com.example.CivicConnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.MobileOtp;

public interface MobileOtpRepository extends JpaRepository<MobileOtp, Long> {

    //Optional<MobileOtp> findTopByUser_UserIdAndVerifiedFalseOrderByOtpIdDesc(Long userId);
    
    Optional<MobileOtp> findTopByUserAndVerifiedFalseOrderByOtpIdDesc(User user);

}
