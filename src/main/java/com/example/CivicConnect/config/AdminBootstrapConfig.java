package com.example.CivicConnect.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.repository.UserRepository;

@Configuration
public class AdminBootstrapConfig {

    @Bean
    CommandLineRunner createAdmin(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {

        return args -> {

            String email = "admin@civicconnect.gov";

            if (userRepository.existsByEmail(email)) {
                return; // already created
            }

            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail(email);
            admin.setMobile("9111111111");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(RoleName.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);

            System.out.println(" ADMIN CREATED SUCCESSFULLY");
        };
    }
}
