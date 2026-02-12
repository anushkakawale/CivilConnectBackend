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
                // ALLOW PREFLIGHT (OPTIONS requests)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 🔓 PUBLIC ENDPOINTS (No authentication required)
                .requestMatchers(
                        "/api/auth/**",
                        "/uploads/**",
                        "/api/images/**",
                        "/api/citizens/register"
                ).permitAll()
                
                // Allow fetching wards/departments publicly
                .requestMatchers(HttpMethod.GET, "/api/wards", "/api/departments").permitAll()
                
                // Restrict CREATING wards/departments to ADMIN only
                .requestMatchers(HttpMethod.POST, "/api/wards", "/api/departments").hasAnyRole("ADMIN")
                
                // 👮 ADMIN ENDPOINTS (Admin can access everything)
                .requestMatchers(HttpMethod.GET, "/api/admin/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/admin/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/admin/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasAnyRole("ADMIN")
               
                // 🏘 WARD OFFICER ENDPOINTS
                .requestMatchers("/api/ward-officer/**").hasAnyAuthority("ROLE_WARD_OFF_ICER", "ROLE_WARD_OFFICER", "ROLE_ADMIN")
                
                // 🏢 DEPARTMENT OFFICER ENDPOINTS (CRITICAL FIX - Allow all HTTP methods with wildcards)
                .requestMatchers(HttpMethod.GET, "/api/department/**").hasAnyRole("DEPARTMENT_OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/department/**").hasAnyRole("DEPARTMENT_OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/department/**").hasAnyRole("DEPARTMENT_OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/department/**").hasAnyRole("DEPARTMENT_OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/department-officer/**").hasAnyRole("DEPARTMENT_OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/department-officer/**").hasAnyRole("DEPARTMENT_OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/department-officer/**").hasAnyRole("DEPARTMENT_OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/department-officer/**").hasAnyRole("DEPARTMENT_OFFICER", "ADMIN")
                
                // 👤 CITIZEN ENDPOINTS
                .requestMatchers(HttpMethod.GET, "/api/citizen/**").hasAnyRole("CITIZEN", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/citizen/**").hasAnyRole("CITIZEN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/citizen/**").hasAnyRole("CITIZEN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/citizen/**").hasAnyRole("CITIZEN", "ADMIN")
                .requestMatchers("/api/citizens/**").hasAnyRole("CITIZEN", "ADMIN")
                
                // 🏢 COMMON PROTECTED ENDPOINTS (All authenticated users: Admin, Officer, Citizen)
                .requestMatchers(
                        "/api/profile/**",
                        "/api/notifications/**",
                        "/api/users/**",
                        "/api/map/**",
                        "/api/complaints/**"
                ).authenticated()
                
                // 🌎 ALL OTHER REQUESTS
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