package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.service.map.ComplaintMapService;

@RestController
@RequestMapping("/api/department/map")
public class DepartmentMapController {

    private final ComplaintMapService mapService;
    private final OfficerProfileRepository officerProfileRepository;

    // ✅ CONSTRUCTOR INJECTION (THIS FIXES EVERYTHING)
    public DepartmentMapController(
            ComplaintMapService mapService,
            OfficerProfileRepository officerProfileRepository) {

        this.mapService = mapService;
        this.officerProfileRepository = officerProfileRepository;
    }

    @GetMapping
    public ResponseEntity<?> departmentView(Authentication auth) {

        User user = (User) auth.getPrincipal();

        OfficerProfile profile = officerProfileRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        return ResponseEntity.ok(
                mapService.departmentMap(
                        profile.getWard().getWardId(),
                        profile.getDepartment().getDepartmentId()
                )
        );
    }
}
