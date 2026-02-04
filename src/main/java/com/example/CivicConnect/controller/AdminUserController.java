package com.example.CivicConnect.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.UserDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final com.example.CivicConnect.repository.OfficerProfileRepository officerProfileRepository;
    private final com.example.CivicConnect.repository.CitizenProfileRepository citizenProfileRepository;

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(
            userRepository.findAll(pageable)
                .map(this::toDto)
        );
    }

    private UserDTO toDto(User u) {
        String ward = "N/A";
        String dept = "N/A";

        if (u.getRole() == com.example.CivicConnect.entity.enums.RoleName.CITIZEN) {
            var profile = citizenProfileRepository.findByUser_UserId(u.getUserId());
            if (profile.isPresent() && profile.get().getWard() != null) {
                ward = profile.get().getWard().getAreaName();
            }
        } else {
            var profile = officerProfileRepository.findByUser_UserId(u.getUserId());
            if (profile.isPresent()) {
                if (profile.get().getWard() != null) ward = profile.get().getWard().getAreaName();
                if (profile.get().getDepartment() != null) dept = profile.get().getDepartment().getName();
            }
        }

        return new UserDTO(
            u.getUserId(),
            u.getName(),
            u.getEmail(),
            u.getMobile(),
            u.getRole().name(),
            u.isActive(),
            ward,
            dept
        );
    }
}
