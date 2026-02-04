package com.example.CivicConnect.dto;

public record AdminComplaintDTO(
    Long id,
    String title,
    String status,
    String ward,
    String department,
    String createdAt
) {}
