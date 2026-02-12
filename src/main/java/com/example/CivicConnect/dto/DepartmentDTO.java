package com.example.CivicConnect.dto;

public record DepartmentDTO(
        Long departmentId,
        String name
) {
    public Long id() {
        return departmentId;
    }
}