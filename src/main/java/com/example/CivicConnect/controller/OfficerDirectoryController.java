package com.example.CivicConnect.controller;

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
@RequestMapping("/api/officers")
@RequiredArgsConstructor
public class OfficerDirectoryController {

    private final OfficerDirectoryService officerDirectoryService;

    /**
     * CITIZEN: Get all officers for their assigned ward
     */
    @GetMapping("/citizen/ward")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<List<OfficerDirectoryDTO>> getOfficersForCitizen(Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(officerDirectoryService.getOfficersForCitizen(citizen));
    }

    /**
     * WARD OFFICER: Get department officers working in their ward
     */
    @GetMapping("/ward-officer/dept-officers")
    @PreAuthorize("hasRole('WARD_OFFICER')")
    public ResponseEntity<List<OfficerDirectoryDTO>> getDeptOfficersForWard(Authentication auth) {
        User wardOfficer = (User) auth.getPrincipal();
        return ResponseEntity.ok(officerDirectoryService.getDepartmentOfficersForWardOfficer(wardOfficer));
    }

    /**
     * DEPARTMENT OFFICER: Get peer officers (same ward and department)
     */
    @GetMapping("/department-officer/peers")
    @PreAuthorize("hasRole('DEPARTMENT_OFFICER')")
    public ResponseEntity<List<OfficerDirectoryDTO>> getPeers(Authentication auth) {
        User deptOfficer = (User) auth.getPrincipal();
        return ResponseEntity.ok(officerDirectoryService.getPeersForDepartmentOfficer(deptOfficer));
    }

    /**
     * ADMIN: Get all officers in the system
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OfficerDirectoryDTO>> getAllOfficers(Authentication auth) {
        User admin = (User) auth.getPrincipal();
        return ResponseEntity.ok(officerDirectoryService.getAllOfficersForAdmin(admin));
    }
}
