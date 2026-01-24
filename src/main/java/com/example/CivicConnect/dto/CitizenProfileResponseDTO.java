package com.example.CivicConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CitizenProfileResponseDTO {

    private String name;
    private String email;
    private String mobile;
    private Long wardId;
    private String wardName;
}