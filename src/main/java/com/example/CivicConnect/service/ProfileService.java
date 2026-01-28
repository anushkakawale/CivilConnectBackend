package com.example.CivicConnect.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    // ===============================
    // UPDATE NAME
    // ===============================
    public void updateName(User user, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }

        user.setName(newName.trim());
        userRepository.save(user);

        notificationService.notifyUser(
                user,
                "Profile Updated",
                "Your name has been successfully updated."
        );
    }

    // ===============================
    // UPDATE PASSWORD
    // ===============================
    public void updatePassword(User user, String currentPassword, String newPassword) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        notificationService.notifyUser(
                user,
                "Password Changed",
                "Your password has been successfully changed."
        );
    }

    // ===============================
    // GET USER PROFILE
    // ===============================
    public User getUserProfile(User user) {
        return userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
