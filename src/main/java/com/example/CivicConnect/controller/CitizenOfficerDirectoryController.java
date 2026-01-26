package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.OfficerDirectoryDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizens/officers")
@RequiredArgsConstructor
public class CitizenOfficerDirectoryController {

    private final OfficerProfileRepository officerRepo;
    private final CitizenProfileRepository citizenRepo;

    // LIST OFFICERS IN MY WARD
    @GetMapping
    public ResponseEntity<?> officers(Authentication auth) {

        User citizen = (User) auth.getPrincipal();

        CitizenProfile profile = citizenRepo
                .findByUser_UserId(citizen.getUserId())
                .orElseThrow();

        if (profile.getWard() == null) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(
                officerRepo.findByWard_WardId(profile.getWard().getWardId())
                        .stream()
                        .map(o -> new OfficerDirectoryDTO(
                                o.getUser().getUserId(),
                                o.getUser().getName(),
                                o.getUser().getEmail(),
                                o.getUser().getMobile(),
                                o.getUser().getRole().name(),
                                o.getDepartment() != null
                                        ? o.getDepartment().getName()
                                        : "Ward Office",
                                o.getWard().getWardNumber()
                        ))

                        .toList()
        );
    }

    // OFFICER DETAILS PAGE
    @GetMapping("/{officerUserId}")
    public ResponseEntity<?> officerDetails(@PathVariable Long officerUserId) {

        OfficerProfile profile = officerRepo
                .findByUser_UserId(officerUserId)
                .orElseThrow();

        return ResponseEntity.ok(
            new OfficerDirectoryDTO(
                profile.getUser().getUserId(),
                profile.getUser().getName(),
                profile.getUser().getMobile(),
                profile.getUser().getEmail(),
                profile.getUser().getRole().name(),
                profile.getDepartment() != null
                    ? profile.getDepartment().getName()
                    : "Ward Office",
                profile.getWard() != null
                    ? profile.getWard().getWardNumber()
                    : null
            )
        );
    }
}
