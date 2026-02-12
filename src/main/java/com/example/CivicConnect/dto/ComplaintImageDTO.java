package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import com.example.CivicConnect.entity.enums.ImageStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintImageDTO {
    private Long imageId;
    private String imageUrl;
    private ImageStage stage;
    private String uploadedBy;
    private String uploadedByRole;
    private LocalDateTime uploadedAt;
}