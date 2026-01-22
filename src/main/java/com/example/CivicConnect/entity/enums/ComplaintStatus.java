package com.example.CivicConnect.entity.enums;

public enum ComplaintStatus {
    SUBMITTED,     // Citizen
    ASSIGNED,      // System
    IN_PROGRESS,   // Department Officer
    RESOLVED,      // Department Officer
    APPROVED,      // Ward Officer
    CLOSED,        // Admin
    REOPENED,      // Citizen
    REJECTED       // Admin (optional, misuse/fake)
}
