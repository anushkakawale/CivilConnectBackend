package com.example.CivicConnect.controller.citizen;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.OfficerDirectoryDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.OfficerDirectoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizen/officers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CITIZEN')")
public class CitizenOfficerDirectoryController {

    private final OfficerDirectoryService officerDirectoryService;

    // 1️⃣ Get ALL officers in my ward (Ward + Dept)
    @GetMapping
    public ResponseEntity<List<OfficerDirectoryDTO>> getMyWardOfficers(Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(officerDirectoryService.getOfficersForCitizen(citizen));
    }

    // 2️⃣ Get ONLY my Ward Officer
    @GetMapping("/ward-officer")
    public ResponseEntity<OfficerDirectoryDTO> getWardOfficer(Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(officerDirectoryService.getWardOfficerForCitizen(citizen));
    }
    
    // 3️⃣ Get ONLY Dept Officers in my ward
    @GetMapping({"/dept-officers", "/department-officers"})
    public ResponseEntity<List<OfficerDirectoryDTO>> getDeptOfficers(Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(officerDirectoryService.getDepartmentOfficersForCitizen(citizen));
    }

    // 4️⃣ Get ALL Dept Officers in the city (if you want to show a general directory)
    @GetMapping("/all-dept-officers")
    public ResponseEntity<List<OfficerDirectoryDTO>> getAllDeptOfficers(Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(officerDirectoryService.getAllDepartmentOfficersForCitizen(citizen));
    }
}
