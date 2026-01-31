package com.example.CivicConnect.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.WardChangeRequestDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.entity.profiles.WardChangeRequest;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.WardRepository;
import com.example.CivicConnect.service.WardChangeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ward-change")
@RequiredArgsConstructor
public class WardChangeController {

    private final WardChangeService wardChangeService;
    private final WardRepository wardRepository;
    private final OfficerProfileRepository officerProfileRepository;

    // ===============================
    // CITIZEN: CREATE WARD CHANGE REQUEST
    // ===============================
    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> createWardChangeRequest(
            @RequestBody Map<String, Long> request,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        
        if (user.getRole() != RoleName.CITIZEN) {
            throw new RuntimeException("Only citizens can request ward changes");
        }
        
        Long newWardId = request.get("wardId");
        wardChangeService.createWardChangeRequest(user, newWardId);
        
        return ResponseEntity.ok(Map.of("message", "Ward change request submitted successfully"));
    }

    // ===============================
    // CITIZEN: VIEW OWN REQUESTS
    // ===============================
    @GetMapping("/my-requests")
    public ResponseEntity<List<WardChangeRequestDTO>> getMyRequests(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        List<WardChangeRequest> requests = wardChangeService.getCitizenRequests(user);
        
        List<WardChangeRequestDTO> dtos = requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    // ===============================
    // WARD OFFICER: VIEW PENDING REQUESTS FOR WARD
    // ===============================
    @GetMapping("/pending")
    public ResponseEntity<List<WardChangeRequestDTO>> getPendingRequests(Authentication auth) {

        User officer = (User) auth.getPrincipal();

        if (officer.getRole() != RoleName.WARD_OFFICER) {
            throw new RuntimeException("Only ward officers can view pending requests");
        }

        OfficerProfile officerProfile =
                officerProfileRepository
                        .findByUser_UserId(officer.getUserId())
                        .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        Ward ward = officerProfile.getWard();

        List<WardChangeRequestDTO> dtos =
                wardChangeService.getPendingForWard(ward)
                        .stream()
                        .map(this::convertToDTO)
                        .toList();

        return ResponseEntity.ok(dtos);
    }


    // ===============================
    // WARD OFFICER: APPROVE REQUEST
    // ===============================
    @PutMapping("/{requestId}/approve")
    public ResponseEntity<Map<String, String>> approveRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> request,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        
        if (user.getRole() != RoleName.WARD_OFFICER) {
            throw new RuntimeException("Only ward officers can approve requests");
        }
        
        String remarks = request.getOrDefault("remarks", "Approved");
        wardChangeService.approveWardChange(requestId, user, remarks);
        
        return ResponseEntity.ok(Map.of("message", "Ward change request approved"));
    }

    // ===============================
    // WARD OFFICER: REJECT REQUEST
    // ===============================
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<Map<String, String>> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> request,
            Authentication auth) {
        
        User user = (User) auth.getPrincipal();
        
        if (user.getRole() != RoleName.WARD_OFFICER) {
            throw new RuntimeException("Only ward officers can reject requests");
        }
        
        String remarks = request.getOrDefault("remarks", "Rejected");
        wardChangeService.rejectWardChange(requestId, user, remarks);
        
        return ResponseEntity.ok(Map.of("message", "Ward change request rejected"));
    }

    // ===============================
    // HELPER: CONVERT TO DTO
    // ===============================
    private WardChangeRequestDTO convertToDTO(WardChangeRequest request) {
        return new WardChangeRequestDTO(
                request.getRequestId(),
                request.getCitizen().getName(),
                request.getCitizen().getEmail(),
                request.getCitizen().getMobile(),
                request.getOldWard() != null ? request.getOldWard().getWardNumber() : null,
                request.getRequestedWard().getWardNumber(),
                request.getStatus().name(),
                request.getRequestedAt().toString(),
                request.getDecidedAt() != null ? request.getDecidedAt().toString() : null,
                request.getDecidedBy() != null ? request.getDecidedBy().getName() : null,
                request.getRemarks()
        );
    }
}
