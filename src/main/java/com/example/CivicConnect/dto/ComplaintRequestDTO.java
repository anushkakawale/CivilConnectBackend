package com.example.CivicConnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ComplaintRequestDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Long departmentId;
}
