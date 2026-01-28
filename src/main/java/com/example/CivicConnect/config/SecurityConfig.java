package com.example.CivicConnect.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.CivicConnect.security.JWTAuthenticationFilter;
@Configuration
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
                // ✅ ALLOW PREFLIGHT
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 🔓 PUBLIC ENDPOINTS
                .requestMatchers(
                        "/api/auth/**",
                        "/api/citizens/register",
                        "/api/wards",
                        "/uploads/**",
                        "/api/images/**"
                ).permitAll()
                // 🔐 PROFILE ENDPOINTS (Authenticated users)
                .requestMatchers("/api/profile/password").authenticated()
                //.requestMatchers("/api/profile/citizen").hasRole("CITIZEN")
                .requestMatchers("/api/profile/officer")
                    .hasAnyRole("WARD_OFFICER", "DEPARTMENT_OFFICER")
                // 🏢 DEPARTMENT OFFICER ENDPOINTS
                .requestMatchers("/api/department/**").hasRole("DEPARTMENT_OFFICER")
                // 🏘 WARD OFFICER ENDPOINTS
                .requestMatchers("/api/ward-officer/**").hasRole("WARD_OFFICER")
                // 🛡 ADMIN ENDPOINTS
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 👤 CITIZEN ENDPOINTS (Specific paths)
                .requestMatchers("/api/complaints/**").authenticated()
                .requestMatchers("/api/notifications/**").authenticated()
                
                // 👤 CITIZEN-ONLY ENDPOINTS (Removed duplicate)
                .requestMatchers("/api/citizens/**").hasRole("CITIZEN")
                // 🔒 ALL OTHER REQUESTS
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