package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDisplayDTO {
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
