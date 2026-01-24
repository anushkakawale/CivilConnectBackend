package com.example.CivicConnect.dto;

import lombok.Data;

@Data
public class OfficerProfileUpdateDTO {
    private Long wardId;
    private Long departmentId; // optional for ward officer
}