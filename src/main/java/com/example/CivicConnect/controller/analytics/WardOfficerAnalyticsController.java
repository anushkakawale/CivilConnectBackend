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
@RequestMapping("/api/ward-officer/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('WARD_OFFICER')")
public class WardOfficerAnalyticsController {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;

    /**
     * Get comprehensive ward analytics dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        Long wardId = profile.getWard().getWardId();

        // Get all complaints in this ward
        List<Complaint> allComplaints = complaintRepository
                .findByWard_WardId(wardId);

        // Overall statistics
        long totalComplaints = allComplaints.size();
        long pending = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED || 
                             c.getStatus() == ComplaintStatus.IN_PROGRESS)
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

        // Pending approvals
        long pendingApprovals = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED)
                .count();

        // Department-wise breakdown
        Map<String, Long> departmentWise = allComplaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment().getName(),
                        Collectors.counting()
                ));

        Map<String, Long> departmentPending = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED || 
                             c.getStatus() == ComplaintStatus.IN_PROGRESS)
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment().getName(),
                        Collectors.counting()
                ));

        Map<String, Long> departmentResolved = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED || 
                             c.getStatus() == ComplaintStatus.APPROVED)
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment().getName(),
                        Collectors.counting()
                ));

        // Calculate department performance
        List<Map<String, Object>> departmentPerformance = departmentWise.entrySet().stream()
                .map(entry -> {
                    String deptName = entry.getKey();
                    long total = entry.getValue();
                    long deptPending = departmentPending.getOrDefault(deptName, 0L);
                    long deptResolved = departmentResolved.getOrDefault(deptName, 0L);
                    double completionRate = total > 0 ? ((double) deptResolved / total) * 100 : 0;

                    return Map.<String, Object>of(
                            "department", deptName,
                            "total", total,
                            "pending", deptPending,
                            "resolved", deptResolved,
                            "completionRate", String.format("%.1f%%", completionRate)
                    );
                })
                .collect(Collectors.toList());

        // SLA statistics
        long slaBreached = allComplaints.stream()
                .filter(c -> c.getSla() != null && 
                        c.getSla().getStatus() == com.example.CivicConnect.entity.enums.SLAStatus.BREACHED)
                .count();

        // Recent activity (last 7 days)
        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
        long recentComplaints = allComplaints.stream()
                .filter(c -> c.getCreatedAt().isAfter(sevenDaysAgo))
                .count();
        long recentClosed = allComplaints.stream()
                .filter(c -> (c.getStatus() == ComplaintStatus.CLOSED) && 
                             c.getUpdatedAt().isAfter(sevenDaysAgo))
                .count();

        // Officer performance in ward
        List<OfficerProfile> deptOfficers = officerProfileRepository
                .findByWard_WardIdAndUser_Role(wardId, com.example.CivicConnect.entity.enums.RoleName.DEPARTMENT_OFFICER);

        List<Map<String, Object>> officerPerformance = deptOfficers.stream()
                .map(officer -> {
                    List<Complaint> officerComplaints = complaintRepository
                            .findByAssignedOfficer_UserId(officer.getUser().getUserId());
                    
                    long officerTotal = officerComplaints.size();
                    long officerResolved = officerComplaints.stream()
                            .filter(c -> c.getStatus() == ComplaintStatus.CLOSED || 
                                         c.getStatus() == ComplaintStatus.APPROVED)
                            .count();
                    
                    return Map.<String, Object>of(
                            "officerName", officer.getUser().getName(),
                            "department", officer.getDepartment().getName(),
                            "totalAssigned", officerTotal,
                            "resolved", officerResolved,
                            "pending", officerTotal - officerResolved
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> response = Map.<String, Object>of(
                "wardName", profile.getWard().getAreaName(),
                "wardOfficer", user.getName(),
                "overallStatistics", Map.<String, Object>of(
                        "totalComplaints", totalComplaints,
                        "pending", pending,
                        "resolved", resolved,
                        "approved", approved,
                        "closed", closed,
                        "pendingApprovals", pendingApprovals,
                        "slaBreached", slaBreached
                ),
                "departmentPerformance", departmentPerformance,
                "officerPerformance", officerPerformance,
                "recentActivity", Map.<String, Object>of(
                        "last7Days", recentComplaints,
                        "closedLast7Days", recentClosed
                )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get work distribution across departments
     */
    @GetMapping("/department-distribution")
    public ResponseEntity<?> getDepartmentDistribution(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        Long wardId = profile.getWard().getWardId();
        List<Complaint> complaints = complaintRepository.findByWard_WardId(wardId);

        Map<String, Map<String, Long>> distribution = complaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment().getName(),
                        Collectors.groupingBy(
                                c -> c.getStatus().name(),
                                Collectors.counting()
                        )
                ));

        return ResponseEntity.ok(distribution);
    }

    /**
     * Get monthly trends for the ward
     */
    @GetMapping("/monthly-trends")
    public ResponseEntity<?> getMonthlyTrends(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        Long wardId = profile.getWard().getWardId();
        
        // Get complaints from last 6 months
        java.time.LocalDateTime sixMonthsAgo = java.time.LocalDateTime.now().minusMonths(6);
        List<Complaint> complaints = complaintRepository
                .findByWard_WardIdAndCreatedAtAfter(wardId, sixMonthsAgo);

        // Group by month
        Map<String, Long> monthlyRegistered = complaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().getYear() + "-" + 
                             String.format("%02d", c.getCreatedAt().getMonthValue()),
                        Collectors.counting()
                ));

        Map<String, Long> monthlyClosed = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED)
                .collect(Collectors.groupingBy(
                        c -> c.getUpdatedAt().getYear() + "-" + 
                             String.format("%02d", c.getUpdatedAt().getMonthValue()),
                        Collectors.counting()
                ));

        return ResponseEntity.ok(Map.<String, Object>of(
                "monthlyRegistered", monthlyRegistered,
                "monthlyClosed", monthlyClosed
        ));
    }
}
