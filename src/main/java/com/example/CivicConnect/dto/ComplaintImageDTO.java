package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintImageDTO {

    private String imageUrl;
    private String imageStage;
    private LocalDateTime uploadedAt;
}