package com.example.CivicConnect.entity.enums;

public enum NotificationType {
    // Complaint lifecycle
    NEW_COMPLAINT,
    COMPLAINT_CREATED,
    APPROVAL_REQUIRED,
    STATUS_UPDATE,
    ASSIGNMENT,
    RESOLVED,
    CLOSED,
    REOPENED,
    
    // SLA related
    SLA_WARNING,
    SLA_BREACHED,
    
    // Other
    FEEDBACK_REQUEST,
    WARD_CHANGE,
    SYSTEM
}