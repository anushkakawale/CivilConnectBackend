package com.example.CivicConnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.CitizenRegistrationDTO;
import com.example.CivicConnect.service.CitizenRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/citizens")
public class CitizenRegistrationController {

    private final CitizenRegistrationService service;

    public CitizenRegistrationController(CitizenRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerCitizen(
            @Valid @RequestBody CitizenRegistrationDTO dto) {

        service.registerCitizen(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Citizen registered successfully");
    }
}
