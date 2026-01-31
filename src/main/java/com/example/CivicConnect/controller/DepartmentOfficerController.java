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
@RequestMapping("/api/department/officers")
@RequiredArgsConstructor
public class DepartmentOfficerController {

    private final OfficerDirectoryService service;

    // 🏢 Department Officer → View his Ward Officer
    @GetMapping("/ward-officer")
    public ResponseEntity<?> wardOfficer(Authentication auth) {

        User officer = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                service.getWardOfficerForDepartmentOfficer(officer)
        );
    }
}
