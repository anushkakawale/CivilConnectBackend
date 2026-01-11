package com.example.CivicConnect.exception;

import java.time.LocalDateTime;

public class ApiResponse {

    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiResponse(String message) {
        this.message = message;
    }

    // getters
}
