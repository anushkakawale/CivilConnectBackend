package com.example.CivicConnect.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.CivicConnect.security.JWTAuthenticationFilter;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JWTAuthenticationFilter jwtFilter;
    public SecurityConfig(JWTAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // ✅ ALLOW PREFLIGHT (OPTIONS requests)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 🔓 PUBLIC ENDPOINTS (No authentication required)
                .requestMatchers(
                        "/api/auth/**",
                        "/api/wards",
                        "/api/departments",
                        "/uploads/**",
                        "/api/images/**",
                        "/api/citizens/register"
                ).permitAll()
                
                // 🛡 ADMIN ENDPOINTS (Must come before generic patterns)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 🏘 WARD OFFICER ENDPOINTS
                .requestMatchers("/api/ward-officer/**").hasRole("WARD_OFFICER")
                
                // 🏢 DEPARTMENT OFFICER ENDPOINTS
                .requestMatchers("/api/department/**").hasRole("DEPARTMENT_OFFICER")
                
                // 👤 CITIZEN ENDPOINTS (Specific - must come before /api/citizens/**)
                .requestMatchers("/api/citizen/**").hasRole("CITIZEN")
                
                
                // 🔐 PROFILE ENDPOINTS (All authenticated users)
                .requestMatchers("/api/profile/**").authenticated()
                
                // 📢 NOTIFICATION ENDPOINTS (All authenticated users)
                .requestMatchers("/api/notifications/**").authenticated()
                
                // 📋 COMPLAINT ENDPOINTS (All authenticated users can view)
                .requestMatchers("/api/complaints/**").authenticated()
                .requestMatchers("/api/images/**").authenticated()
                .requestMatchers("/api/complaints/map").authenticated()

                // 🔒 ALL OTHER REQUESTS (Require authentication)
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
}