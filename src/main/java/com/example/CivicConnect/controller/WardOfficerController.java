package com.example.CivicConnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.DepartmentOfficerRegistrationDTO;
import com.example.CivicConnect.service.DepartmentOfficerRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ward-officer")
public class WardOfficerController {
	//creating department officer
    private final DepartmentOfficerRegistrationService service;

    public WardOfficerController(DepartmentOfficerRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/register/department-officer")
    public ResponseEntity<?> registerDepartmentOfficer(
            @Valid @RequestBody DepartmentOfficerRegistrationDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registerDepartmentOfficer(dto));
    }


}

