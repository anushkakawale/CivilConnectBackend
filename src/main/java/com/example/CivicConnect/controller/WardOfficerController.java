package com.example.CivicConnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.DepartmentOfficerRegistrationDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.service.DepartmentOfficerRegistrationService;
import com.example.CivicConnect.service.citizen.WardChangeApprovalService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/ward-officer")
public class WardOfficerController {

    private final DepartmentOfficerRegistrationService departmentOfficerService;
    private final WardChangeApprovalService wardChangeApprovalService;
    private final OfficerProfileRepository officerProfileRepository;

    public WardOfficerController(
            DepartmentOfficerRegistrationService departmentOfficerService,
            WardChangeApprovalService wardChangeApprovalService,
            OfficerProfileRepository officerProfileRepository) {

        this.departmentOfficerService = departmentOfficerService;
        this.wardChangeApprovalService = wardChangeApprovalService;
        this.officerProfileRepository = officerProfileRepository;
    }

    // REGISTER DEPARTMENT OFFICER
    @PostMapping("/register/department-officer")
    public ResponseEntity<?> registerDepartmentOfficer(
            @Valid @RequestBody DepartmentOfficerRegistrationDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(departmentOfficerService.registerDepartmentOfficer(dto));
    }

    // APPROVE WARD CHANGE
    @PutMapping("/ward-change/{requestId}/approve")
    public ResponseEntity<?> approveWardChange(
            @PathVariable Long requestId,
            Authentication auth) {

        User officer = (User) auth.getPrincipal();
        wardChangeApprovalService.approveWardChange(requestId, officer);
        return ResponseEntity.ok("Ward change approved successfully");
    }

    // ✅ LIST DEPARTMENT OFFICERS UNDER WARD
    @GetMapping("/department-officers")
    public ResponseEntity<?> departmentOfficers(Authentication auth) {

        User wardOfficer = (User) auth.getPrincipal();

        OfficerProfile profile = officerProfileRepository
                .findByUser_UserId(wardOfficer.getUserId())
                .orElseThrow();

        return ResponseEntity.ok(
            officerProfileRepository
                .findByWard_WardId(profile.getWard().getWardId())
                .stream()
                .filter(o -> o.getUser().getRole() == RoleName.DEPARTMENT_OFFICER)
                .toList()
        );
    }
}
