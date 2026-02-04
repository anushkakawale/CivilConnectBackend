package com.example.CivicConnect.dto;

public record WardDepartmentOfficerDTO(
    Long userId,
    String name,
    String department,
    String email,
    String mobile
) {}
