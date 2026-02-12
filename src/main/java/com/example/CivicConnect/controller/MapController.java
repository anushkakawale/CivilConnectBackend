package com.example.CivicConnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintMapDTO;
import com.example.CivicConnect.dto.OfficerDirectoryDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.service.MapComplaintService;

import lombok.RequiredArgsConstructor;

/**
 * Unified Map Controller for tracking complaint status and locations
 * Handles role-based visibility automatically
 */
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MapController {

    private final MapComplaintService mapService;

    /**
     * Get complaints for map view based on user role
     * Optional status filter
     */
    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintMapDTO>> getMapComplaints(
            Authentication auth,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long wardId,
            @RequestParam(defaultValue = "false") boolean myComplaintsOnly) {
        
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(mapService.getMapComplaints(user, status, departmentId, wardId, myComplaintsOnly));
    }

    /**
     * Get ward boundaries with statistics for Admin/Analytical map
     */
    @GetMapping("/wards/boundaries")
    public ResponseEntity<List<com.example.CivicConnect.dto.WardMapDTO>> getWardBoundaries() {
        return ResponseEntity.ok(mapService.getWardBoundaries());
    }

    /**
     * Get all citizen locations for Admin global view
     */
    @GetMapping("/admin/citizens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.example.CivicConnect.dto.CitizenMapDTO>> getCitizenLocations() {
        return ResponseEntity.ok(mapService.getCitizenLocations());
    }

    /**
     * Get only active complaints (ASSIGNED, IN_PROGRESS) for current user scope
     */
    @GetMapping("/active-complaints")
    public ResponseEntity<List<ComplaintMapDTO>> getActiveComplaints(Authentication auth) {
        User user = (User) auth.getPrincipal();
        // We can filter by status IN [ASSIGNED, IN_PROGRESS] - for now just get filtered list
        List<ComplaintMapDTO> all = mapService.getMapComplaints(user, null);
        List<ComplaintMapDTO> active = all.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED || c.getStatus() == ComplaintStatus.IN_PROGRESS)
                .toList();
        return ResponseEntity.ok(active);
    }

    /**
     * Get complaints grouped by status for map view tracking
     * Returns all statuses with counts and marker data
     */
    @GetMapping("/complaints/grouped")
    public ResponseEntity<Map<String, Object>> getMapComplaintsGrouped(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(mapService.getMapComplaintsGrouped(user));
    }

    /**
     * Get complaint status tracking statistics for current scope
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getTrackingStatistics(Authentication auth) {
        User user = (User) auth.getPrincipal();
        Map<String, Object> data = mapService.getMapComplaintsGrouped(user);
        @SuppressWarnings("unchecked")
        Map<String, Long> counts = (Map<String, Long>) data.get("statusCounts");
        return ResponseEntity.ok(counts);
    }

    /**
     * Get officer directory based on user role for map overlay/directory
     */
    @GetMapping("/officers")
    public ResponseEntity<List<OfficerDirectoryDTO>> getOfficerDirectory(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(mapService.getOfficerDirectory(user));
    }
}
