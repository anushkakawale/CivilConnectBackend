package com.example.CivicConnect.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapViewController {

    private final ComplaintRepository complaintRepository;

    /**
     * Get active complaints for map view (only ASSIGNED and IN_PROGRESS)
     * Excludes RESOLVED, APPROVED, and CLOSED complaints
     */
    @GetMapping("/active-complaints")
    public ResponseEntity<?> getActiveComplaints(
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long departmentId) {

        List<Complaint> complaints;

        // Filter by ward and department if provided
        if (wardId != null && departmentId != null) {
            complaints = complaintRepository
                    .findByWard_WardIdAndDepartment_DepartmentIdAndStatusIn(
                            wardId, 
                            departmentId,
                            List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS)
                    );
        } else if (wardId != null) {
            complaints = complaintRepository
                    .findByWard_WardIdAndStatusIn(
                            wardId,
                            List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS)
                    );
        } else if (departmentId != null) {
            complaints = complaintRepository
                    .findByDepartment_DepartmentIdAndStatusIn(
                            departmentId,
                            List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS)
                    );
        } else {
            // Get all active complaints
            complaints = complaintRepository
                    .findByStatusIn(List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));
        }

        // Map to response format
        List<Map<String, Object>> complaintMarkers = complaints.stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .filter(c -> c.getLatitude() != 0.0 && c.getLongitude() != 0.0)
                .map(c -> {
                    Map<String, Object> marker = new java.util.HashMap<>();
                    marker.put("complaintId", c.getComplaintId());
                    marker.put("title", c.getTitle());
                    marker.put("description", c.getDescription().length() > 100 
                            ? c.getDescription().substring(0, 100) + "..." 
                            : c.getDescription());
                    marker.put("status", c.getStatus().name());
                    marker.put("priority", c.getPriority().name());
                    marker.put("latitude", c.getLatitude());
                    marker.put("longitude", c.getLongitude());
                    marker.put("wardName", c.getWard().getAreaName());
                    marker.put("departmentName", c.getDepartment().getName());
                    marker.put("createdAt", c.getCreatedAt());
                    marker.put("assignedOfficer", c.getAssignedOfficer() != null 
                            ? c.getAssignedOfficer().getName() 
                            : "Not Assigned");
                    marker.put("slaStatus", c.getSla() != null ? c.getSla().getStatus().name() : "UNKNOWN");
                    return marker;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.<String, Object>of(
                "count", complaintMarkers.size(),
                "complaints", complaintMarkers
        ));
    }

    /**
     * Get complaint statistics by status for map legend
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getMapStatistics(
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long departmentId) {

        List<Complaint> allComplaints;

        if (wardId != null && departmentId != null) {
            allComplaints = complaintRepository
                    .findByWard_WardIdAndDepartment_DepartmentId(wardId, departmentId);
        } else if (wardId != null) {
            allComplaints = complaintRepository.findByWard_WardId(wardId);
        } else if (departmentId != null) {
            allComplaints = complaintRepository.findByDepartment_DepartmentId(departmentId);
        } else {
            allComplaints = complaintRepository.findAll();
        }

        long assigned = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED)
                .count();
        long inProgress = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS)
                .count();
        long resolved = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED)
                .count();
        long approved = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.APPROVED)
                .count();
        long closed = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED)
                .count();

        return ResponseEntity.ok(Map.<String, Object>of(
                "total", allComplaints.size(),
                "assigned", assigned,
                "inProgress", inProgress,
                "resolved", resolved,
                "approved", approved,
                "closed", closed,
                "activeOnMap", assigned + inProgress
        ));
    }

    /**
     * Get hotspot areas (areas with most complaints)
     */
    @GetMapping("/hotspots")
    public ResponseEntity<?> getHotspots() {
        List<Complaint> activeComplaints = complaintRepository
                .findByStatusIn(List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));

        // Group by ward
        Map<String, Long> wardHotspots = activeComplaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getWard().getAreaName(),
                        Collectors.counting()
                ));

        // Sort by count descending
        List<Map<String, Object>> hotspots = wardHotspots.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .map(entry -> Map.<String, Object>of(
                        "wardName", entry.getKey(),
                        "activeComplaints", entry.getValue()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.<String, Object>of(
                "hotspots", hotspots
        ));
    }
}
