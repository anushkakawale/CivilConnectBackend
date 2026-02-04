package com.example.CivicConnect.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountService {

    private final UserRepository userRepository;

    public void deactivate(User user) {
        user.setActive(false);
        userRepository.save(user);
    }
    
    public void deactivateUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }
}
