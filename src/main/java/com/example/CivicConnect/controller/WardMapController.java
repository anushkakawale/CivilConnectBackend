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
@RequestMapping("/api/ward/map")
public class WardMapController {

    private final OfficerProfileRepository officerProfileRepository;
    private final ComplaintMapService mapService;

    public WardMapController(
            OfficerProfileRepository officerProfileRepository,
            ComplaintMapService mapService) {

        this.officerProfileRepository = officerProfileRepository;
        this.mapService = mapService;
    }

    @GetMapping
    public ResponseEntity<?> wardView(Authentication auth) {

        User user = (User) auth.getPrincipal();

        OfficerProfile profile = officerProfileRepository
                .findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        return ResponseEntity.ok(
                mapService.wardMap(profile.getWard().getWardId())
        );
    }
}
