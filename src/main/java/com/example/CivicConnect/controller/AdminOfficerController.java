package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.WardOfficerRegistrationDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.service.WardOfficerRegistrationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminOfficerController {

	private final WardOfficerRegistrationService service;
	private final OfficerProfileRepository officerProfileRepository;

	@PostMapping("/register/ward-officer")
    public ResponseEntity<?> registerWardOfficer(
            @RequestBody WardOfficerRegistrationDTO dto,
            Authentication auth) {

        User admin = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                service.registerWardOfficer(dto, admin)
        );
    }

	@GetMapping("/officers")
	public ResponseEntity<?> allOfficers() {
		return ResponseEntity.ok(officerProfileRepository.findAll());
	}
}