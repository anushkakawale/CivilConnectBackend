package com.example.CivicConnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.CivicConnect.dto.CitizenRegistrationDTO;
import com.example.CivicConnect.dto.CitizenRegistrationResponseDTO;
import com.example.CivicConnect.service.CitizenRegistrationService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/citizens")
public class CitizenRegistrationController {

    private final CitizenRegistrationService service;

    public CitizenRegistrationController(
            CitizenRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<CitizenRegistrationResponseDTO> registerCitizen(
            @Valid @RequestBody CitizenRegistrationDTO dto) {

        log.info("Received registration request for {}", dto.getEmail());

        CitizenRegistrationResponseDTO response =
                service.registerCitizen(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
