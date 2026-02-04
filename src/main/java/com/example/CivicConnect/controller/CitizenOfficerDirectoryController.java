package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.OfficerDirectoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizens/officers")
@RequiredArgsConstructor
public class CitizenOfficerDirectoryController {

    private final OfficerDirectoryService officerDirectoryService;

    // LIST OFFICERS IN MY WARD
    @GetMapping
    public ResponseEntity<?> officers(Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(officerDirectoryService.getOfficersForCitizen(citizen));
    }

    // OFFICER DETAILS PAGE
    @GetMapping("/{officerUserId}")
    public ResponseEntity<?> officerDetails(@PathVariable Long officerUserId) {
        return ResponseEntity.ok(officerDirectoryService.getOfficerDetails(officerUserId));
    }
}
