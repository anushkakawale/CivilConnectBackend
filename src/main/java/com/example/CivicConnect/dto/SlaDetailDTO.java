package com.example.CivicConnect.dto;

import java.time.LocalDateTime;
import com.example.CivicConnect.entity.enums.SLAStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaDetailDTO {
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private LocalDateTime completionTime;
    private Long remainingHours;
    private Long totalHoursAllocated;
    private boolean breached;
    private SLAStatus status;
    private boolean escalated;
    private String priority;
}
