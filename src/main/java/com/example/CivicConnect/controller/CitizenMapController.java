package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.service.map.ComplaintMapService;

@RestController
@RequestMapping("/api/citizens/map")
public class CitizenMapController {

    private final ComplaintMapService mapService;
    private final CitizenProfileRepository citizenProfileRepository;

    // ✅ CONSTRUCTOR INJECTION
    public CitizenMapController(
            ComplaintMapService mapService,
            CitizenProfileRepository citizenProfileRepository) {

        this.mapService = mapService;
        this.citizenProfileRepository = citizenProfileRepository;
    }

    @GetMapping("/ward")
    public ResponseEntity<?> myWard(Authentication auth) {

        User user = (User) auth.getPrincipal();

        Long wardId = citizenProfileRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Citizen profile not found"))
                .getWard()
                .getWardId();

        return ResponseEntity.ok(mapService.wardMap(wardId));
    }
}
