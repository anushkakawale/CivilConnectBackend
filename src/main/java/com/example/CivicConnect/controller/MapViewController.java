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
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapViewController {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;

    /**
     * Get active complaints for map view (only ASSIGNED and IN_PROGRESS)
     */
    @GetMapping("/active-complaints")
    public ResponseEntity<?> getActiveComplaints(
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long departmentId) {

        List<Complaint> complaints;

        if (wardId != null && departmentId != null) {
            complaints = complaintRepository.findByWard_WardIdAndDepartment_DepartmentIdAndStatusIn(
                    wardId, departmentId, List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));
        } else if (wardId != null) {
            complaints = complaintRepository.findByWard_WardIdAndStatusIn(
                    wardId, List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));
        } else if (departmentId != null) {
            complaints = complaintRepository.findByDepartment_DepartmentIdAndStatusIn(
                    departmentId, List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));
        } else {
            complaints = complaintRepository.findByStatusIn(List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));
        }

        List<Map<String, Object>> markers = complaints.stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .map(c -> {
                    Map<String, Object> marker = new java.util.HashMap<>();
                    marker.put("complaintId", c.getComplaintId());
                    marker.put("title", c.getTitle());
                    marker.put("status", c.getStatus().name());
                    marker.put("latitude", c.getLatitude());
                    marker.put("longitude", c.getLongitude());
                    marker.put("wardName", c.getWard() != null ? c.getWard().getAreaName() : "N/A");
                    marker.put("departmentName", c.getDepartment() != null ? c.getDepartment().getName() : "N/A");
                    return marker;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("count", markers.size(), "complaints", markers));
    }

    /**
     * Role-based scoped map view
     */
    @GetMapping("/my-scope")
    public ResponseEntity<?> getMyScopeComplaints(org.springframework.security.core.Authentication auth) {
        User user = (User) auth.getPrincipal();

        if (user.getRole() == com.example.CivicConnect.entity.enums.RoleName.ADMIN) {
            return getActiveComplaints(null, null);
        }

        var profileOpt = officerProfileRepository.findByUser_UserId(user.getUserId());
        if (profileOpt.isEmpty()) return getActiveComplaints(null, null);

        var profile = profileOpt.get();
        Long wardId = profile.getWard() != null ? profile.getWard().getWardId() : null;
        Long deptId = profile.getDepartment() != null ? profile.getDepartment().getDepartmentId() : null;

        if (user.getRole() == com.example.CivicConnect.entity.enums.RoleName.WARD_OFFICER) {
            return getActiveComplaints(wardId, null);
        } else if (user.getRole() == com.example.CivicConnect.entity.enums.RoleName.DEPARTMENT_OFFICER) {
            return getActiveComplaints(wardId, deptId);
        }
        return getActiveComplaints(null, null);
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getMapStatistics(
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long departmentId) {
        
        List<Complaint> all;
        if (wardId != null && departmentId != null) {
            all = complaintRepository.findByWard_WardIdAndDepartment_DepartmentId(wardId, departmentId);
        } else if (wardId != null) {
            all = complaintRepository.findByWard_WardId(wardId);
        } else if (departmentId != null) {
            all = complaintRepository.findByDepartment_DepartmentId(departmentId);
        } else {
            all = complaintRepository.findAll();
        }

        Map<String, Long> stats = all.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));
        
        return ResponseEntity.ok(stats);
    }
}
