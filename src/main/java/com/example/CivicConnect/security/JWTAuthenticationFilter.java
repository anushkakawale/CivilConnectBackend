package com.example.CivicConnect.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.service.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);

        if (email != null &&
            SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()
                        );

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,   // ✅ YOUR ENTITY
                                null,
                                List.of(authority)
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}

//@Component
//@RequiredArgsConstructor
//public class JWTAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JWTService jwtService;
//    private final UserDetailsService userDetailsService;
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String token = authHeader.substring(7);
//        if (token.isBlank()) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String email;
//        try {
//            email = jwtService.extractEmail(token);
//        } catch (Exception e) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        if (email != null &&
//            SecurityContextHolder.getContext().getAuthentication() == null) {
//
//            UserDetails userDetails =
//                    ((UserDetailsService) userDetails).loadUserByUsername(email);
//
//            if (jwtService.isTokenValid(token)) {
//            	SimpleGrantedAuthority authority =
//                        new SimpleGrantedAuthority(
//                                "ROLE_" + userDetails.getRole().name()
//                        );
//                UsernamePasswordAuthenticationToken authToken =
//                        new UsernamePasswordAuthenticationToken(
//                                userDetails,
//                                null,
//                                //userDetails.getAuthorities());
//                                Collections.singletonList(authority);
//
//                authToken.setDetails(
//                        new WebAuthenticationDetailsSource()
//                                .buildDetails(request));
//
//                SecurityContextHolder.getContext()
//                        .setAuthentication(authToken);
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
//@Component
//@RequiredArgsConstructor
//public class JWTAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JWTService jwtService;
//    private final UserRepository userRepository;
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String token = authHeader.substring(7);
//
//        if (!jwtService.isTokenValid(token)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String email = jwtService.extractEmail(token);
//
//        if (email != null &&
//            SecurityContextHolder.getContext().getAuthentication() == null) {
//
//            User user = userRepository.findByEmail(email)
//                    .orElse(null);
//
//            if (user != null) {
//
//                SimpleGrantedAuthority authority =
//                        new SimpleGrantedAuthority(
//                                "ROLE_" + user.getRole().name()
//                        );
//
//                UsernamePasswordAuthenticationToken authToken =
//                        new UsernamePasswordAuthenticationToken(
//                                user.getEmail(),               // ✅ YOUR ENTITY
//                                null,
//                                Collections.singletonList(authority)
//                        );
//
//                SecurityContextHolder.getContext()
//                        .setAuthentication(authToken);
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
