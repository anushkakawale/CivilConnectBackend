package com.example.CivicConnect.dto;

import lombok.Data;

@Data
public class WardChangeRequestPayload {
    private Long wardId;
    private String reason; // Optional reason for the change
}
