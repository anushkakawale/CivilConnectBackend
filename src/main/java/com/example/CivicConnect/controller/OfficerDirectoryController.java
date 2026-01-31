/*
package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.OfficerDirectoryDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.service.OfficerDirectoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/officers")
@RequiredArgsConstructor
public class OfficerDirectoryController {

    private final OfficerDirectoryService officerDirectoryService;

    // ===============================
    // ADMIN: GET ALL OFFICERS
    // ===============================
    @GetMapping("/all")
    public ResponseEntity<List<OfficerDirectoryDTO>> getAllOfficers(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        if (user.getRole() != RoleName.ADMIN) {
            throw new RuntimeException("Only admins can view all officers");
        }
        
        List<OfficerDirectoryDTO> officers = officerDirectoryService.getAllOfficers();
        return ResponseEntity.ok(officers);
    }

    // ===============================
    // ADMIN/CITIZEN: GET WARD OFFICERS
    // ===============================
    @GetMapping("/ward-officers")
    public ResponseEntity<List<OfficerDirectoryDTO>> getWardOfficers() {
        List<OfficerDirectoryDTO> officers = officerDirectoryService.getAllWardOfficers();
        return ResponseEntity.ok(officers);
    }

    // ===============================
    // ADMIN/WARD OFFICER: GET DEPARTMENT OFFICERS
    // ===============================
    @GetMapping("/department-officers")
    public ResponseEntity<List<OfficerDirectoryDTO>> getDepartmentOfficers() {
        List<OfficerDirectoryDTO> officers = officerDirectoryService.getAllDepartmentOfficers();
        return ResponseEntity.ok(officers);
    }
}*/
