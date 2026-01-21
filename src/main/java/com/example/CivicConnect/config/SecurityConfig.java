package com.example.CivicConnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.CivicConnect.security.JWTAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JWTAuthenticationFilter jwtFilter;

    public SecurityConfig(JWTAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                		"/api/auth/**",
                		"/api/citizens/register"
                		).permitAll()
                .requestMatchers(
                		"/api/citizens/**"
                		).hasRole("CITIZEN")
                .requestMatchers(
                		"/api/ward-officer/**"
                		).hasRole("WARD_OFFICER")
                .requestMatchers(
                		"/api/department/**"
                		).hasRole("DEPARTMENT_OFFICER")
                .requestMatchers(
                		"/api/admin/**"
                		).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
