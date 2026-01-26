package com.example.CivicConnect.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
//
//@Service
//public class JWTService {
//
//    private final SecretKey key;
//
//    @Value("${jwt.expiration}")
//    private long expiration;
//
//    public JWTService(@Value("${jwt.secret}") String secret) {
//        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//    }
//
//    public String generateToken(User user) {
//        return Jwts.builder()
//                .setSubject(user.getEmail())
//                .claim("role", user.getRole().name())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(key)
//                .compact();
//    }
//
//    public String extractEmail(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    public boolean isTokenValid(String token) {
//        try {
//            Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token); // validates signature + expiry
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//}
@Service
public class JWTService {

    private final SecretKey key;

    @Value("${jwt.expiration}")
    private long expiration;

    public JWTService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", "ROLE_" + user.getRole().name())
                .claim("userId", user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ SIMPLE + SAFE
    public boolean isTokenValid(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
