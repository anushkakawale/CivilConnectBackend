package com.example.CivicConnect.dto;

public record AdminOfficerDTO(
    Long userId,
    String name,
    String role,
    String wardName,
    String departmentName,
    String email,
    String mobile,
    boolean active,
    String createdAt
) {}
