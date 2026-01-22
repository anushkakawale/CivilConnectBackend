package com.example.CivicConnect.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.AdminProfile;
import com.example.CivicConnect.repository.AdminProfileRepository;
import com.example.CivicConnect.repository.UserRepository;

@Configuration
public class AdminBootstrapConfig {

    @Bean
    CommandLineRunner createAdmin(UserRepository userRepository,
                                  AdminProfileRepository adminProfileRepository,
                                  PasswordEncoder passwordEncoder) {

        return args -> {

            String email = "admin@civicconnect.gov";

            User admin;

            // 🔹 CASE 1: USER EXISTS
            if (userRepository.existsByEmail(email)) {
                admin = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Admin user not found"));
            }
            // 🔹 CASE 2: USER DOES NOT EXIST
            else {
                admin = new User();
                admin.setName("System Admin");
                admin.setEmail(email);
                admin.setMobile("9111111111");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole(RoleName.ADMIN);
                admin.setActive(true);

                admin = userRepository.save(admin);
                System.out.println("ADMIN USER CREATED");
            }

            // 🔹 ENSURE ADMIN PROFILE EXISTS
            if (!adminProfileRepository.existsByUser(admin)) {

                AdminProfile adminProfile = new AdminProfile();
                adminProfile.setUser(admin);

                adminProfileRepository.save(adminProfile);
                System.out.println("ADMIN PROFILE CREATED");
            }

            System.out.println("ADMIN BOOTSTRAP COMPLETED");
        };
    }
}
