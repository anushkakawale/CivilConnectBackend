// dto/CitizenRegistrationResponseDTO.java
package com.example.CivicConnect.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitizenRegistrationResponseDTO {
    private Long userId;
    private String name;
//    private String email;
//    private String mobile;
    private String message;
}