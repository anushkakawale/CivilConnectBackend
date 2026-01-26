package com.example.CivicConnect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {

    @Min(1)
    @Max(5)
    private int rating;

    @NotBlank(message = "Comment cannot be blank")
    private String comment;
    
    // Getter for comment to match the entity field name
    public String getComment() {
        return comment;
    }
}
