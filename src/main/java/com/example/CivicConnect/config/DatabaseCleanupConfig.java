package com.example.CivicConnect.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Database cleanup configuration to fix foreign key constraint issues
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanupConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner cleanupOrphanedNotifications() {
        return args -> {
            try {
                log.info("🧹 Checking for orphaned notifications...");
                
                // Delete notifications where user_id doesn't exist in users table
                String deleteSql = "DELETE FROM notifications WHERE user_id NOT IN (SELECT user_id FROM users)";
                int deletedCount = jdbcTemplate.update(deleteSql);
                
                if (deletedCount > 0) {
                    log.warn("⚠️ Deleted {} orphaned notifications", deletedCount);
                } else {
                    log.info("✅ No orphaned notifications found");
                }
                
            } catch (Exception e) {
                log.error("❌ Error during notification cleanup: {}", e.getMessage());
                // Don't throw - allow application to continue
            }
        };
    }
}
