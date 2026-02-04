package com.example.CivicConnect.dto;

public record AdminOfficerDTO(
    Long userId,
    String name,
    String role,
    String ward,
    String department,
    String email,
    boolean active
) {}
