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

        Map<String, Object> response = Map.<String, Object>of(
                "officerName", user.getName(),
                "department", profile.getDepartment().getName(),
                "ward", profile.getWard().getAreaName(),
                "statistics", Map.<String, Object>of(
                        "totalAssigned", totalAssigned,
                        "pending", pending,
                        "inProgress", inProgress,
                        "resolved", resolved,
                        "approved", approved,
                        "closed", closed,
                        "completionRate", String.format("%.1f%%", completionRate),
                        "avgResolutionTimeHours", String.format("%.1f", avgResolutionTime)
                ),
                "sla", Map.<String, Object>of(
                        "breached", slaBreached,
                        "warning", slaWarning,
                        "onTrack", totalAssigned - slaBreached - slaWarning
                ),
                "recentActivity", Map.<String, Object>of(
                        "last7Days", recentComplaints,
                        "resolvedLast7Days", recentResolved
                )
        );

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

        // Group by month
        Map<String, Long> monthlyAssigned = complaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().getMonth().name(),
                        Collectors.counting()
                ));

        Map<String, Long> monthlyResolved = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED || 
                             c.getStatus() == ComplaintStatus.APPROVED)
                .collect(Collectors.groupingBy(
                        c -> c.getUpdatedAt().getMonth().name(),
                        Collectors.counting()
                ));

        return ResponseEntity.ok(Map.<String, Object>of(
                "monthlyAssigned", monthlyAssigned,
                "monthlyResolved", monthlyResolved
        ));
    }
}
