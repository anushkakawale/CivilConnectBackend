package com.example.CivicConnect.controller.analytics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/department/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DEPARTMENT_OFFICER')")
public class DepartmentAnalyticsController {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;

    /**
     * Get analytics dashboard for department officer
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        // Get all complaints assigned to this officer
        List<Complaint> allComplaints = complaintRepository
                .findByAssignedOfficer_UserId(user.getUserId());

        // Calculate statistics
        long totalAssigned = allComplaints.size();
        long pending = allComplaints.stream()
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

        // SLA statistics
        long slaBreached = allComplaints.stream()
                .filter(c -> c.getSla() != null && 
                        c.getSla().getStatus() == com.example.CivicConnect.entity.enums.SLAStatus.BREACHED)
                .count();
        long slaWarning = allComplaints.stream()
                .filter(c -> c.getSla() != null && 
                        c.getSla().getStatus() == com.example.CivicConnect.entity.enums.SLAStatus.WARNING)
                .count();

        // Completion rate
        double completionRate = totalAssigned > 0 
                ? ((double) (approved + closed) / totalAssigned) * 100 
                : 0;

        // Average resolution time (in hours) for completed complaints
        double avgResolutionTime = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED || 
                             c.getStatus() == ComplaintStatus.APPROVED)
                .mapToLong(c -> java.time.Duration.between(
                        c.getCreatedAt(), 
                        c.getUpdatedAt()
                ).toHours())
                .average()
                .orElse(0);

        // Recent activity (last 7 days)
        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
        long recentComplaints = allComplaints.stream()
                .filter(c -> c.getCreatedAt().isAfter(sevenDaysAgo))
                .count();
        long recentResolved = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED && 
                             c.getUpdatedAt().isAfter(sevenDaysAgo))
                .count();

        // 6️⃣ WARD OFFICER DETAILS (NEW)
        OfficerProfile wardOfficerProfile = officerProfileRepository
                .findFirstByWard_WardIdAndUser_RoleAndActiveTrue(
                        profile.getWard().getWardId(), 
                        com.example.CivicConnect.entity.enums.RoleName.WARD_OFFICER
                ).orElse(null);

        Map<String, Object> wardOfficerDetails = new java.util.HashMap<>();
        if (wardOfficerProfile != null) {
            wardOfficerDetails.put("name", wardOfficerProfile.getUser().getName());
            wardOfficerDetails.put("email", wardOfficerProfile.getUser().getEmail());
            wardOfficerDetails.put("mobile", wardOfficerProfile.getUser().getMobile());
        } else {
            wardOfficerDetails.put("message", "No ward officer assigned");
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("officerName", user.getName());
        response.put("department", profile.getDepartment() != null ? profile.getDepartment().getName() : "N/A");
        response.put("ward", profile.getWard() != null ? profile.getWard().getAreaName() : "N/A");
        response.put("wardOfficer", wardOfficerDetails);
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalAssigned", totalAssigned);
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("resolved", resolved);
        stats.put("approved", approved);
        stats.put("closed", closed);
        stats.put("completionRate", String.format("%.1f%%", completionRate));
        stats.put("avgResolutionTimeHours", String.format("%.1f", avgResolutionTime));
        response.put("statistics", stats);
        
        Map<String, Object> sla = new java.util.HashMap<>();
        sla.put("breached", slaBreached);
        sla.put("warning", slaWarning);
        sla.put("onTrack", totalAssigned - slaBreached - slaWarning);
        response.put("sla", sla);
        
        Map<String, Object> recent = new java.util.HashMap<>();
        recent.put("last7Days", recentComplaints);
        recent.put("resolvedLast7Days", recentResolved);
        response.put("recentActivity", recent);

        return ResponseEntity.ok(response);
    }


    /**
     * Get pending work details
     */
    @GetMapping("/pending-work")
    public ResponseEntity<?> getPendingWork(Authentication auth) {
        User user = (User) auth.getPrincipal();

        List<Complaint> pendingComplaints = complaintRepository
                .findByAssignedOfficer_UserIdAndStatusIn(
                        user.getUserId(),
                        List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS)
                );

        List<Map<String, Object>> pendingList = pendingComplaints.stream()
                .map(c -> Map.<String, Object>of(
                        "complaintId", c.getComplaintId(),
                        "title", c.getTitle(),
                        "status", c.getStatus().name(),
                        "priority", c.getPriority().name(),
                        "createdAt", c.getCreatedAt(),
                        "slaDeadline", c.getSla() != null ? c.getSla().getSlaDeadline() : null,
                        "SLAStatus", c.getSla() != null ? c.getSla().getStatus().name() : null,
                        "daysOpen", java.time.Duration.between(
                                c.getCreatedAt(), 
                                java.time.LocalDateTime.now()
                        ).toDays()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.<String, Object>of(
                "count", pendingList.size(),
                "complaints", pendingList
        ));
    }

    /**
     * Get performance trends (monthly)
     */
    @GetMapping("/trends")
    public ResponseEntity<?> getTrends(Authentication auth) {
        User user = (User) auth.getPrincipal();

        // Get complaints from last 6 months
        java.time.LocalDateTime sixMonthsAgo = java.time.LocalDateTime.now().minusMonths(6);
        List<Complaint> complaints = complaintRepository
                .findByAssignedOfficer_UserIdAndCreatedAtAfter(user.getUserId(), sixMonthsAgo);

        // Group by Year-Month (YYYY-MM)
        Map<String, Long> monthlyAssigned = complaints.stream()
                .collect(Collectors.groupingBy(
                        c -> String.format("%d-%02d", c.getCreatedAt().getYear(), c.getCreatedAt().getMonthValue()),
                        Collectors.counting()
                ));

        Map<String, Long> monthlyResolved = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED || 
                             c.getStatus() == ComplaintStatus.APPROVED)
                .collect(Collectors.groupingBy(
                        c -> String.format("%d-%02d", c.getUpdatedAt().getYear(), c.getUpdatedAt().getMonthValue()),
                        Collectors.counting()
                ));

        return ResponseEntity.ok(Map.<String, Object>of(
                "monthlyAssigned", monthlyAssigned,
                "monthlyResolved", monthlyResolved
        ));
    }
    /**
     * Get all complaints assigned to this officer's ward and department (Office Registry)
     */
    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedToMyOffice(Authentication auth) {
        User user = (User) auth.getPrincipal();

        // 1. Fetch the officer's profile to get their specific Ward and Department
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        if (profile.getWard() == null || profile.getDepartment() == null) {
            return ResponseEntity.badRequest().body("Officer is not assigned to a specific ward/department office.");
        }

        // 2. Fetch complaints matching the officer's Ward and Department
        List<Complaint> complaints = complaintRepository.findByWard_WardIdAndDepartment_DepartmentId(
                profile.getWard().getWardId(),
                profile.getDepartment().getDepartmentId()
        );

        // 3. Transform to a flat JSON structure for the frontend table
        List<Map<String, Object>> responseList = complaints.stream()
                .map(c -> {
                    Map<String, Object> task = new java.util.HashMap<>();
                    task.put("complaintId", c.getComplaintId());
                    task.put("title", c.getTitle());
                    task.put("status", c.getStatus().name());
                    task.put("priority", c.getPriority().name());
                    task.put("createdAt", c.getCreatedAt());
                    
                    // Include SLA info if available
                    if (c.getSla() != null) {
                        task.put("slaStatus", c.getSla().getStatus().name());
                        task.put("slaDeadline", c.getSla().getSlaDeadline());
                    } else {
                        task.put("slaStatus", "OK");
                    }
                    
                    return task;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
