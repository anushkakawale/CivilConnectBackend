package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ProfileResponseDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.UserProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/department/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DEPARTMENT_OFFICER')")
public class DepartmentProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<ProfileResponseDTO> getProfile(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(userProfileService.getProfile(user));
    }
}
