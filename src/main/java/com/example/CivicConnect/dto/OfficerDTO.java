package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficerDTO {
    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private String departmentName;
    private String wardName;
}
