package com.example.CivicConnect.dto;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.enums.RoleName;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
	private Long userId;
    private String name;
    private String email;
    private String mobile;
    private RoleName role;
    private boolean active;
    private LocalDateTime createdAt;
}
