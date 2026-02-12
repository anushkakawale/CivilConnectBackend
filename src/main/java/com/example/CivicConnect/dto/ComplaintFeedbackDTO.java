package com.example.CivicConnect.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintFeedbackDTO {
    
    @NotNull(message = "Complaint ID is required")
    private Long complaintId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
    private String comment;
}
