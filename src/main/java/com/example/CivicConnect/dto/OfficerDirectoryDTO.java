package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficerDirectoryDTO {
	private Long userId;
    private String name;
    private String mobile;
    private String email;
    private String role;
    private String department;
    private String wardNumber;
}
