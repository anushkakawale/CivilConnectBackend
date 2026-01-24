package com.example.CivicConnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.DepartmentOfficerRegistrationDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.DepartmentOfficerRegistrationService;
import com.example.CivicConnect.service.citizen.WardChangeApprovalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ward-officer")
public class WardOfficerController {

    // 1️⃣ Department Officer Registration
    private final DepartmentOfficerRegistrationService departmentOfficerService;

    // 2️⃣ Ward Change Approval
    private final WardChangeApprovalService wardChangeApprovalService;

    public WardOfficerController(
            DepartmentOfficerRegistrationService departmentOfficerService,
            WardChangeApprovalService wardChangeApprovalService) {

        this.departmentOfficerService = departmentOfficerService;
        this.wardChangeApprovalService = wardChangeApprovalService;
    }

    // =====================================================
    // REGISTER DEPARTMENT OFFICER
    // =====================================================
    @PostMapping("/register/department-officer")
    public ResponseEntity<?> registerDepartmentOfficer(
            @Valid @RequestBody DepartmentOfficerRegistrationDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(departmentOfficerService.registerDepartmentOfficer(dto));
    }

    // =====================================================
    // APPROVE WARD CHANGE REQUEST (CITIZEN)
    // =====================================================
    @PutMapping("/ward-change/{requestId}/approve")
    public ResponseEntity<?> approveWardChange(
            @PathVariable Long requestId,
            Authentication auth) {

        User officer = (User) auth.getPrincipal();

        wardChangeApprovalService.approveWardChange(requestId, officer);

        return ResponseEntity.ok("Ward change approved successfully");
    }
}
