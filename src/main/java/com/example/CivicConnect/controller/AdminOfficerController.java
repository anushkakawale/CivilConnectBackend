package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.WardOfficerRegistrationDTO;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.service.WardOfficerRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminOfficerController {
	//admin creating ward officer
    private final WardOfficerRegistrationService service;
    
    public AdminOfficerController(WardOfficerRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/register/ward-officer")
    public ResponseEntity<?> registerWardOfficer(
            @Valid @RequestBody WardOfficerRegistrationDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registerWardOfficer(dto));
    }


}
