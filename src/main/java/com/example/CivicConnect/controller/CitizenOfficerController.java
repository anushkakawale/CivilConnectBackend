package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.OfficerDirectoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizen/officers")
@RequiredArgsConstructor
public class CitizenOfficerController {

    private final OfficerDirectoryService officerDirectoryService;
    @GetMapping("/ward-officer")
    public ResponseEntity<?> wardOfficer(Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                officerDirectoryService.getWardOfficerForCitizen(citizen)
        );
    }
    @GetMapping("/department-officers")
    public ResponseEntity<?> departmentOfficers(Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                officerDirectoryService.getDepartmentOfficersForCitizen(citizen)
        );
    }



}
