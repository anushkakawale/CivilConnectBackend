package com.example.CivicConnect.dto;

import com.example.CivicConnect.entity.enums.RoleName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
	private String token;
	private RoleName role;
	private Long userId;
//	private String name;
//	private String email;
	public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }
}